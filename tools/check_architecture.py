#!/usr/bin/env python3
"""Architecture-as-Context drift checker (MP 10.5, WP R1.3).

Static, offline, read-only. Compares architecture/context-map.yaml, the module/feature
docs, dependency-rules.md and the real Gradle files (settings.gradle.kts, */build.gradle.kts).
Never modifies source or docs; only writes the report files given by --json / --md.

Exit code: 1 if any ERROR finding, 0 otherwise (WARN allowed), 2 on usage/environment failure.

`--boundaries-only` (MP:R2.1 / KD-02 / MP:BLD-01) runs just the module-boundary checks: every
project edge declared in a module build file (any configuration: api, implementation, ksp, test*,
string-form, `path =` form) must be listed in the allowed-edge block of dependency-rules.md; no
`:app` consumers, no `:testing` in production configurations, no edge to a non-included module,
no cycles, and no dependency form the checker cannot parse (fail-closed). It needs neither PyYAML
nor the docs, so it is fast enough for a pre-commit/CI gate.

Usage: python3 tools/check_architecture.py [--root DIR] [--json PATH] [--md PATH] [--boundaries-only]
"""
from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

try:
    import yaml  # PyYAML
except ImportError:  # pragma: no cover
    yaml = None

ERROR, WARN, INFO = "ERROR", "WARN", "INFO"
DOC_STATUS = {"DRAFT", "VERIFIED", "NEEDS_REFRESH"}
TEST_STATUS = {"NOT_RUN", "TESTED", "FAILED", "BLOCKED"}
LINK_DOCS = ["context-protocol.md", "module-map.md", "dependency-rules.md"]


class Report:
    def __init__(self) -> None:
        self.findings: list[dict] = []

    def add(self, sev: str, check: str, subject: str, msg: str) -> None:
        self.findings.append({"severity": sev, "check": check, "subject": subject, "message": msg})

    def count(self, sev: str) -> int:
        return sum(1 for f in self.findings if f["severity"] == sev)


# ---------------------------------------------------------------- parsing helpers

def strip_noise(text: str) -> str:
    """Remove HTML comments and fenced code blocks (keeps line structure irrelevant)."""
    text = re.sub(r"<!--.*?-->", "", text, flags=re.S)
    text = re.sub(r"^(```|~~~).*?^\1[^\n]*$", "", text, flags=re.S | re.M)
    return text


def required_headings(template: Path) -> list[tuple[str, str]]:
    """[(number, title)] from `## N. Title` lines of a TEMPLATE (outside comments/fences)."""
    out = []
    for m in re.finditer(r"^##\s+(\d+)\.\s+(.+?)\s*$", strip_noise(template.read_text(encoding="utf-8")), re.M):
        out.append((m.group(1), m.group(2)))
    return out


def norm(s: str) -> str:
    return re.sub(r"\s+", " ", s.strip().lower())


def parse_settings(settings: Path) -> tuple[list[str], list[str]]:
    text = settings.read_text(encoding="utf-8")
    text = re.sub(r"//[^\n]*", "", text)
    includes = re.findall(r'\binclude\(\s*"([^"]+)"', text)
    builds = re.findall(r'\bincludeBuild\(\s*"([^"]+)"', text)
    return includes, builds


def module_dir(root: Path, gradle_path: str) -> Path:
    return root / gradle_path.strip(":").replace(":", "/")


# `implementation(project(":x"))`, `"kspTest"(project(":x"))`, `api(project(path = ":x"))`, `add("api", project(":x"))`
_EDGE_RE = re.compile(
    r'(?:\b(\w+)|"(\w+)")\s*\(\s*project\(\s*(?:path\s*=\s*)?"(:[^"]+)"\s*(?:,[^)]*)?\)\s*\)')
_ADD_RE = re.compile(r'\badd\(\s*"(\w+)"\s*,\s*project\(\s*(?:path\s*=\s*)?"(:[^"]+)"\s*(?:,[^)]*)?\)\s*\)')


_WRAPPERS = {"platform", "enforcedPlatform", "testFixtures", "project", "files", "dep"}


def _is_test_conf(conf: str) -> bool:
    return conf.lower().startswith(("test", "androidtest")) or "Test" in conf


def strip_gradle_comments(text: str) -> str:
    """Remove Kotlin comments, string-aware: `//` and `/* */` (nestable) count only outside "..." / \"\"\"...\"\"\" / '.'
    literals, so a `/*` or `//` inside a string can never hide a later project edge. String contents are kept
    verbatim (edges are string arguments). Newlines inside comments are preserved for line numbers."""
    out: list[str] = []
    i, n = 0, len(text)
    while i < n:
        c = text[i]
        two = text[i:i + 2]
        if text.startswith('"""', i):
            j = text.find('"""', i + 3)
            j = n if j < 0 else j + 3
            while j < n and text[j] == '"':  # closing run of 4+ quotes: extra quotes belong to the content
                j += 1
            out.append(text[i:j])
            i = j
        elif c == '"' or c == "'":
            j = i + 1
            while j < n and text[j] != c and text[j] != "\n":
                j += 2 if text[j] == "\\" else 1
            j = min(j + 1, n)
            out.append(text[i:j])
            i = j
        elif two == "//":
            j = text.find("\n", i)
            i = n if j < 0 else j  # keep the newline itself
        elif two == "/*":
            depth, j = 1, i + 2
            while j < n and depth:
                if text.startswith("/*", j):
                    depth, j = depth + 1, j + 2
                elif text.startswith("*/", j):
                    depth, j = depth - 1, j + 2
                else:
                    j += 1
            out.append("\n" * text.count("\n", i, j))
            i = j
        else:
            out.append(c)
            i += 1
    return "".join(out)


def parse_build_edges_ex(build_file: Path) -> tuple[set[str], set[str], list[str]]:
    """(production edges, test-only edges, unparseable project references)."""
    prod: set[str] = set()
    test: set[str] = set()
    unparsed: list[str] = []
    if not build_file.exists():
        return prod, test, unparsed
    text = strip_gradle_comments(build_file.read_text(encoding="utf-8"))
    matched_spans: list[tuple[int, int]] = []
    for m in _EDGE_RE.finditer(text):
        conf, target = (m.group(1) or m.group(2)), m.group(3)
        if conf in _WRAPPERS:  # implementation(platform(project(":x"))): configuration is the outer call -> unparseable
            continue
        (test if _is_test_conf(conf) else prod).add(target)
        matched_spans.append(m.span())
    for m in _ADD_RE.finditer(text):
        conf, target = m.group(1), m.group(2)
        (test if _is_test_conf(conf) else prod).add(target)
        matched_spans.append(m.span())
    # fail closed: any other `project(` call or type-safe `projects.` accessor is an edge we cannot classify
    for m in re.finditer(r'\bproject\(|\bprojects\.\w', text):
        if not any(a <= m.start() < b for a, b in matched_spans):
            line = text.count("\n", 0, m.start()) + 1
            unparsed.append(f"line {line}: {text[m.start():m.start() + 60].splitlines()[0]}")
    return prod, test, unparsed


def parse_build_edges(build_file: Path) -> tuple[set[str], set[str]]:
    """(production edges, test-only edges) from project(":x") declarations."""
    prod, test, _ = parse_build_edges_ex(build_file)
    return prod, test


def parse_allowed(rules: Path) -> tuple[dict[str, set[str]], dict[str, set[str]]]:
    """Machine-checkable fenced blocks, skipping any section whose heading mentions PROPOSED."""
    text = rules.read_text(encoding="utf-8")
    sections: list[str] = []  # split on headings, ignoring '#' lines inside fenced blocks
    cur: list[str] = []
    in_fence = False
    for line in text.splitlines(keepends=True):
        if line.startswith("```"):
            in_fence = not in_fence
        elif not in_fence and re.match(r"#{1,6}\s", line):
            sections.append("".join(cur))
            cur = []
        cur.append(line)
    sections.append("".join(cur))
    prod: dict[str, set[str]] = {}
    test: dict[str, set[str]] = {}
    for sec in sections:
        head = sec.splitlines()[0] if sec.strip() else ""
        if "PROPOSED" in head.upper():
            continue
        for block in re.findall(r"^```[^\n]*\n(.*?)^```", sec, flags=re.S | re.M):
            is_test = "test-only" in block.lower()
            target = test if is_test else prod
            for line in block.splitlines():
                m = re.match(r"^\s*(:[\w:\-]+)\s*->\s*(.+?)\s*$", line)
                if not m:
                    continue
                src, rhs = m.group(1), m.group(2)
                dests = set() if rhs.startswith("(none)") else {d.strip() for d in rhs.split(",") if d.strip()}
                target.setdefault(src, set()).update(dests)
    return prod, test


def find_cycle(graph: dict[str, set[str]]) -> list[str] | None:
    state: dict[str, int] = {}
    stack: list[str] = []

    def visit(n: str) -> list[str] | None:
        state[n] = 1
        stack.append(n)
        for d in sorted(graph.get(n, ())):
            if state.get(d, 0) == 1:
                return stack[stack.index(d):] + [d]
            if state.get(d, 0) == 0:
                c = visit(d)
                if c:
                    return c
        stack.pop()
        state[n] = 2
        return None

    for n in sorted(graph):
        if state.get(n, 0) == 0:
            c = visit(n)
            if c:
                return c
    return None


# ---------------------------------------------------------------- checks

def check_docs_headings(rep: Report, root: Path, entry: dict, kind: str, req: list[tuple[str, str]]) -> None:
    doc = entry.get("doc")
    subj = f"{kind}:{entry.get('id')}"
    if not doc:
        rep.add(ERROR, "doc-exists", subj, "no `doc` field in context-map")
        return
    p = root / doc
    if not p.is_file():
        rep.add(ERROR, "doc-exists", subj, f"doc file missing: {doc}")
        return
    text = p.read_text(encoding="utf-8")
    body = strip_noise(text)
    found = {(m.group(1), norm(m.group(2))) for m in re.finditer(r"^##\s+(\d+)\.\s+(.+?)\s*$", body, re.M)}
    found_titles = {t for _, t in found}
    for num, title in req:
        if (num, norm(title)) in found:
            continue
        if norm(title) in found_titles:
            rep.add(WARN, "doc-headings", subj, f"{doc}: heading '{title}' present but not numbered {num}")
        else:
            rep.add(ERROR, "doc-headings", subj, f"{doc}: missing required heading '## {num}. {title}'")
    # header fields
    m = re.search(r"^Status:\s*(\S+)", text, re.M)
    if not m:
        rep.add(ERROR, "doc-status", subj, f"{doc}: missing 'Status:' line")
    else:
        val = m.group(1).strip("`*")
        if val not in DOC_STATUS:
            rep.add(ERROR, "doc-status", subj, f"{doc}: Status '{val}' not in {sorted(DOC_STATUS)}")
        elif entry.get("doc_status") and val != entry["doc_status"]:
            rep.add(WARN, "doc-status", subj, f"{doc}: Status '{val}' != context-map doc_status '{entry['doc_status']}'")
    m = re.search(r"^Test status:\s*(\S+)(.*)$", text, re.M)
    if not m:
        rep.add(ERROR, "test-status", subj, f"{doc}: missing 'Test status:' line")
    else:
        val = m.group(1).strip("`*")
        if val not in TEST_STATUS:
            rep.add(ERROR, "test-status", subj, f"{doc}: Test status '{val}' not in {sorted(TEST_STATUS)}")
        else:
            if entry.get("test_status") and val != entry["test_status"]:
                rep.add(WARN, "test-status", subj, f"{doc}: Test status '{val}' != context-map test_status '{entry['test_status']}'")
            if val in ("TESTED", "FAILED"):
                ev = re.search(r"evidence:\s*([^\s)]+)", m.group(2))
                if not ev or not (root / ev.group(1)).exists():
                    rep.add(ERROR, "test-status", subj, f"{doc}: Test status {val} without existing evidence path")


def check_status_fields(rep: Report, root: Path, entry: dict, kind: str) -> None:
    subj = f"{kind}:{entry.get('id')}"
    ds, ts, ev = entry.get("doc_status"), entry.get("test_status"), entry.get("evidence")
    if ds not in DOC_STATUS:
        rep.add(ERROR, "doc-status", subj, f"context-map doc_status '{ds}' not in {sorted(DOC_STATUS)}")
    if ts not in TEST_STATUS:
        rep.add(ERROR, "test-status", subj, f"context-map test_status '{ts}' not in {sorted(TEST_STATUS)}")
    if ts in ("TESTED", "FAILED"):
        if not ev:
            rep.add(ERROR, "test-status", subj, f"test_status {ts} requires an evidence path")
        elif not str(ev).startswith("reports/"):
            rep.add(ERROR, "test-status", subj, f"test_status {ts} evidence must be under reports/: {ev}")
        elif not (root / ev).exists():
            rep.add(ERROR, "test-status", subj, f"test_status {ts} evidence path does not exist: {ev}")
    elif ev and not (root / str(ev)).exists():
        rep.add(WARN, "test-status", subj, f"evidence path does not exist: {ev}")


def kotlin_files(roots: list[Path]) -> list[Path]:
    files: list[Path] = []
    for r in roots:
        if r.is_dir():
            files += sorted(r.rglob("*.kt")) + sorted(r.rglob("*.java"))
    return files


def check_symbols(rep: Report, root: Path, entry: dict, kind: str, roots: list[Path]) -> None:
    subj = f"{kind}:{entry.get('id')}"
    syms = entry.get("entry_symbols") or []
    if not syms:
        rep.add(WARN, "entry-symbols", subj, "no entry_symbols listed")
        return
    files = kotlin_files(roots)
    texts = {f: f.read_text(encoding="utf-8", errors="replace") for f in files}
    for s in syms:
        decl = re.compile(r"\b(?:class|interface|object|fun|typealias)\s+(?:<[^>]*>\s*)?(?:[\w.<>?, ]+\.)?" + re.escape(s) + r"\b")
        prop = re.compile(r"\b(?:val|var)\s+(?:<[^>]*>\s*)?(?:[\w.<>?, ]+\.)?" + re.escape(s) + r"\b")
        hits = [f for f, t in texts.items() if decl.search(t)]
        if not hits:
            phits = [f for f, t in texts.items() if prop.search(t)]
            if phits:
                rep.add(INFO, "entry-symbols", subj, f"'{s}' declared only as val/var in {phits[0].relative_to(root)}")
            else:
                rep.add(ERROR, "entry-symbols", subj, f"entry symbol '{s}' not found in source_roots")
        elif len(set(hits)) > 1:
            names = ", ".join(str(h.relative_to(root)) for h in sorted(set(hits))[:3])
            rep.add(WARN, "entry-symbols", subj, f"entry symbol '{s}' ambiguous: declared in {len(set(hits))} files ({names})")


def check_links(rep: Report, root: Path) -> None:
    arch = root / "architecture"
    files = sorted((arch / "modules").glob("*.md")) + sorted((arch / "features").glob("*.md"))
    files += [arch / n for n in LINK_DOCS]
    for f in files:
        if f.name == "TEMPLATE.md" or not f.is_file():
            continue
        body = strip_noise(f.read_text(encoding="utf-8"))
        body = re.sub(r"`[^`\n]*`", "", body)  # inline code is not a link
        for m in re.finditer(r"\[[^\]]*\]\(([^)\s]+)(?:\s+\"[^\"]*\")?\)", body):
            href = m.group(1)
            if re.match(r"^[a-z][a-z0-9+.-]*:", href, re.I) or href.startswith("#"):
                continue
            target = href.split("#", 1)[0]
            if not target:
                continue
            if not (f.parent / target).exists():
                rep.add(ERROR, "links", str(f.relative_to(root)), f"broken relative link: {href}")


def check_edges(rep: Report, root: Path, includes: list[str], by_path: dict, stats: dict, check_map: bool) -> None:
    """KD-02 boundary check. With check_map=False only the build files vs. allowed-edge block are compared."""
    rules = root / "architecture" / "dependency-rules.md"
    if not rules.is_file():
        rep.add(ERROR, "edges", "dependency-rules.md", "architecture/dependency-rules.md missing")
        return
    allowed_prod, allowed_test = parse_allowed(rules)
    if not allowed_prod:
        rep.add(ERROR, "edges", "dependency-rules.md", "no machine-checkable allowed-edge block parsed")
    actual_prod: dict[str, set[str]] = {}
    for gp in includes:
        bf = module_dir(root, gp) / "build.gradle.kts"
        if not bf.is_file():
            rep.add(ERROR, "edges", gp, f"build file missing: {bf.relative_to(root)}")
            continue
        prod, test, unparsed = parse_build_edges_ex(bf)
        actual_prod[gp] = prod
        for u in unparsed:
            rep.add(ERROR, "edges", gp, f"unparseable project dependency form ({u}); use implementation(project(\":x\")) or extend the checker")
        if gp not in allowed_prod:
            rep.add(ERROR, "edges", gp, "module has no row in the allowed-edge table")
        for t in sorted(prod - allowed_prod.get(gp, set())):
            rep.add(ERROR, "edges", gp, f"edge {gp} -> {t} not allowed by dependency-rules.md")
        for t in sorted(test - allowed_test.get(gp, set())):
            rep.add(ERROR, "edges", gp, f"test edge {gp} -> {t} not allowed (test-only block)")
        for t in sorted(prod | test):
            if t == ":app":
                rep.add(ERROR, "edges", gp, f"{gp} -> :app is forbidden (DEP-01)")
            if t not in includes:
                rep.add(ERROR, "edges", gp, f"{gp} -> {t}: target is not a Gradle module in settings")
        if ":testing" in prod:
            rep.add(ERROR, "edges", gp, f"{gp} -> :testing in a production configuration (BLD-03)")
        m = by_path.get(gp) if check_map else None
        if m is not None:
            cm_dep, cm_test = set(m.get("depends_on") or []), set(m.get("test_depends_on") or [])
            if cm_dep != prod:
                rep.add(ERROR, "edges", gp, f"context-map depends_on != build file (map-only: {sorted(cm_dep - prod)}, build-only: {sorted(prod - cm_dep)})")
            if cm_test != test:
                rep.add(ERROR, "edges", gp, f"context-map test_depends_on != build file (map-only: {sorted(cm_test - test)}, build-only: {sorted(test - cm_test)})")
    for gp in allowed_prod:
        if gp not in includes:
            rep.add(WARN, "edges", gp, "allowed-edge table row for module not in settings.gradle.kts")
    # root build script must not smuggle in cross-module edges (e.g. subprojects { dependencies { ... } })
    rb = root / "build.gradle.kts"
    if rb.is_file():
        _, _, root_unparsed = parse_build_edges_ex(rb)
        rp, rt, _ = parse_build_edges_ex(rb)
        for u in root_unparsed:
            rep.add(ERROR, "edges", "build.gradle.kts", f"root build script references a project dependency ({u})")
        for t in sorted(rp | rt):
            rep.add(ERROR, "edges", "build.gradle.kts", f"root build script declares a project edge -> {t}")
    cyc = find_cycle(actual_prod)
    if cyc:
        rep.add(ERROR, "edges", "graph", "dependency cycle: " + " -> ".join(cyc))
    stats["edges"] = sum(len(v) for v in actual_prod.values())


def run_boundaries(root: Path) -> tuple[Report, dict]:
    """KD-02 only: build-file project edges vs. dependency-rules.md allowed block. No YAML, no docs checks."""
    rep = Report()
    stats: dict = {}
    settings = root / "settings.gradle.kts"
    if not settings.is_file():
        rep.add(ERROR, "settings", "settings.gradle.kts", "settings.gradle.kts missing")
        return rep, stats
    includes, _ = parse_settings(settings)
    stats["gradle_modules"] = len(includes)
    check_edges(rep, root, includes, {}, stats, check_map=False)
    return rep, stats


def run(root: Path) -> tuple[Report, dict]:
    rep = Report()
    arch = root / "architecture"
    stats: dict = {}

    cm_path = arch / "context-map.yaml"
    if not cm_path.is_file():
        rep.add(ERROR, "context-map", "context-map.yaml", "architecture/context-map.yaml missing")
        return rep, stats
    cm = yaml.safe_load(cm_path.read_text(encoding="utf-8")) or {}
    modules = cm.get("modules") or []
    features = cm.get("features") or []
    stats["modules"], stats["features"] = len(modules), len(features)

    # 0. rule/top-level paths
    for key in ("root_architecture", "protocol"):
        if cm.get(key) and not (root / cm[key]).exists():
            rep.add(ERROR, "paths", f"context-map.{key}", f"path missing: {cm[key]}")
    for r in cm.get("rules") or []:
        if not (root / r).exists():
            rep.add(ERROR, "paths", "context-map.rules", f"path missing: {r}")

    # 1. settings.gradle.kts modules <-> context-map
    settings = root / "settings.gradle.kts"
    if not settings.is_file():
        rep.add(ERROR, "settings", "settings.gradle.kts", "settings.gradle.kts missing")
        includes, builds = [], []
    else:
        includes, builds = parse_settings(settings)
    stats["gradle_modules"] = len(includes)
    by_path = {m.get("gradle_path"): m for m in modules}
    for gp in includes:
        if gp not in by_path:
            rep.add(ERROR, "module-coverage", gp, "Gradle module has no context-map entry")
    for b in builds:
        if f"includeBuild:{b}" not in by_path:
            rep.add(ERROR, "module-coverage", f"includeBuild:{b}", "included build has no context-map entry")
    for gp, m in by_path.items():
        if gp in includes:
            continue
        if gp and gp.startswith("includeBuild:") and gp.split(":", 1)[1] in builds:
            continue
        rep.add(ERROR, "module-coverage", str(gp), "context-map module not present in settings.gradle.kts")

    mod_tpl = arch / "modules" / "TEMPLATE.md"
    feat_tpl = arch / "features" / "TEMPLATE.md"
    mod_req = required_headings(mod_tpl) if mod_tpl.is_file() else []
    feat_req = required_headings(feat_tpl) if feat_tpl.is_file() else []
    if not mod_req:
        rep.add(ERROR, "doc-headings", "modules/TEMPLATE.md", "template missing or has no '## N.' headings")
    if not feat_req:
        rep.add(ERROR, "doc-headings", "features/TEMPLATE.md", "template missing or has no '## N.' headings")

    # 2-4,6. per-module / per-feature
    for kind, entries, req in (("module", modules, mod_req), ("feature", features, feat_req)):
        for e in entries:
            subj = f"{kind}:{e.get('id')}"
            roots = []
            for key in ("source_roots", "test_roots"):
                for r in e.get(key) or []:
                    p = root / r
                    if not p.exists():
                        rep.add(ERROR, "paths", subj, f"{key} path missing: {r}")
                    elif key == "source_roots":
                        roots.append(p)
            check_status_fields(rep, root, e, kind)
            check_docs_headings(rep, root, e, kind, req)
            if kind == "module":
                check_symbols(rep, root, e, kind, roots)
        # feature symbols live in the owning module's source roots
    mod_by_id = {m.get("id"): m for m in modules}
    for e in features:
        subj = f"feature:{e.get('id')}"
        owner = mod_by_id.get(e.get("owning_module"))
        if not owner:
            rep.add(ERROR, "feature", subj, f"owning_module '{e.get('owning_module')}' not in context-map")
            continue
        roots = [root / r for r in owner.get("source_roots") or [] if (root / r).exists()]
        check_symbols(rep, root, e, "feature", roots)
        for dep in e.get("depends_on_modules") or []:
            if dep not in mod_by_id:
                rep.add(ERROR, "feature", subj, f"depends_on_modules references unknown module '{dep}'")

    # 5. edges
    check_edges(rep, root, includes, by_path, stats, check_map=True)

    # 7. links
    check_links(rep, root)
    return rep, stats


# ---------------------------------------------------------------- output

def render_md(rep: Report, stats: dict, root: Path) -> str:
    by_check: dict[str, dict[str, int]] = {}
    for f in rep.findings:
        by_check.setdefault(f["check"], {ERROR: 0, WARN: 0, INFO: 0})[f["severity"]] += 1
    lines = ["# Architecture check report", "",
             "Generated by `tools/check_architecture.py` (read-only static check; MP 10.5).", "",
             f"- Result: **{'FAIL' if rep.count(ERROR) else 'PASS'}**  (ERROR {rep.count(ERROR)}, WARN {rep.count(WARN)}, INFO {rep.count(INFO)})",
             f"- Scope: {stats.get('modules', 0)} context-map modules, {stats.get('gradle_modules', 0)} Gradle include(...) modules, "
             f"{stats.get('features', 0)} features, {stats.get('edges', 0)} production project edges", "",
             "## Counts by check", "", "| Check | ERROR | WARN | INFO |", "|---|---|---|---|"]
    for c in sorted(by_check):
        d = by_check[c]
        lines.append(f"| {c} | {d[ERROR]} | {d[WARN]} | {d[INFO]} |")
    for sev in (ERROR, WARN, INFO):
        items = [f for f in rep.findings if f["severity"] == sev]
        lines += ["", f"## {sev} ({len(items)})", ""]
        if not items:
            lines.append("None.")
        for f in items:
            lines.append(f"- [{f['check']}] `{f['subject']}`: {f['message']}")
    return "\n".join(lines) + "\n"


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(description=__doc__.splitlines()[0])
    ap.add_argument("--root", default=None, help="repository root (default: parent of tools/)")
    ap.add_argument("--json", dest="json_path")
    ap.add_argument("--md", dest="md_path")
    ap.add_argument("--boundaries-only", action="store_true",
                    help="only the KD-02 module-boundary check (no PyYAML needed); exit 1 on any forbidden/unlisted/unparseable edge")
    args = ap.parse_args(argv)
    if yaml is None and not args.boundaries_only:
        print("check_architecture: PyYAML is required (pip install pyyaml); no fallback parser is bundled.", file=sys.stderr)
        return 2
    root = Path(args.root).resolve() if args.root else Path(__file__).resolve().parent.parent
    rep, stats = run_boundaries(root) if args.boundaries_only else run(root)

    if args.boundaries_only:
        print(f"Boundary check (KD-02): ERROR {rep.count(ERROR)}  WARN {rep.count(WARN)}  "
              f"({stats.get('gradle_modules', 0)} modules, {stats.get('edges', 0)} production edges)")
    else:
        print(f"Architecture check: ERROR {rep.count(ERROR)}  WARN {rep.count(WARN)}  INFO {rep.count(INFO)}  "
              f"({stats.get('modules', 0)} modules, {stats.get('features', 0)} features, {stats.get('edges', 0)} edges)")
    by_check: dict[str, int] = {}
    for f in rep.findings:
        if f["severity"] == ERROR:
            by_check[f["check"]] = by_check.get(f["check"], 0) + 1
    for c, n in sorted(by_check.items()):
        print(f"  ERROR {c}: {n}")
    for f in rep.findings:
        if f["severity"] in (ERROR, WARN):
            print(f"  {f['severity']:5} [{f['check']}] {f['subject']}: {f['message']}")

    if args.json_path:
        p = Path(args.json_path)
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(json.dumps({"summary": {"error": rep.count(ERROR), "warn": rep.count(WARN), "info": rep.count(INFO), **stats},
                                 "findings": rep.findings}, indent=2) + "\n", encoding="utf-8")
    if args.md_path:
        p = Path(args.md_path)
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(render_md(rep, stats, root), encoding="utf-8")
    return 1 if rep.count(ERROR) else 0


if __name__ == "__main__":
    sys.exit(main())
