"""Self-test for check_architecture.py. Run: python3 -m unittest tools/test_check_architecture.py"""
import sys
import tempfile
import unittest
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import check_architecture as ca  # noqa: E402

TEMPLATE = "# MODULE\n## 1. Identity\n## 2. Contracts\n"
RULES = "## Machine-checkable\n```text\n:a -> (none)\n:b -> :a\n```\n## PROPOSED\n```text\n:a -> :b\n```\n"
CMAP = """schema_version: 1
modules:
  - {id: a, gradle_path: ":a", kind: jvm, doc: architecture/modules/a.md, source_roots: [a/src], test_roots: [],
     entry_symbols: [Foo], depends_on: [], doc_status: DRAFT, test_status: NOT_RUN, evidence: x}
  - {id: b, gradle_path: ":b", kind: jvm, doc: architecture/modules/b.md, source_roots: [b/src], test_roots: [],
     entry_symbols: [Bar], depends_on: [":a"], doc_status: DRAFT, test_status: NOT_RUN, evidence: x}
features: []
"""


def doc(extra=""):
    return "# MODULE: x\nStatus: DRAFT\nTest status: NOT_RUN\n## 1. Identity\n" + extra


def make_fixture(root: Path, b_build: str, b_doc: str):
    def w(rel, text):
        p = root / rel
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text(text)
    w("settings.gradle.kts", 'include(":a")\ninclude(":b")\n')
    w("architecture/context-map.yaml", CMAP)
    w("architecture/modules/TEMPLATE.md", TEMPLATE)
    w("architecture/features/TEMPLATE.md", TEMPLATE)
    w("architecture/dependency-rules.md", RULES)
    w("a/build.gradle.kts", "")
    w("b/build.gradle.kts", b_build)
    w("a/src/Foo.kt", "class Foo\n")
    w("b/src/Bar.kt", "object Bar\n")
    w("x", "")
    w("architecture/modules/a.md", doc("## 2. Contracts\n"))
    w("architecture/modules/b.md", b_doc)


class CheckTest(unittest.TestCase):
    def run_fixture(self, b_build, b_doc):
        with tempfile.TemporaryDirectory() as d:
            make_fixture(Path(d), b_build, b_doc)
            rep, _ = ca.run(Path(d))
        return rep

    def msgs(self, rep, check):
        return [f["message"] for f in rep.findings if f["check"] == check and f["severity"] == ca.ERROR]

    def test_clean_fixture_has_no_errors(self):
        rep = self.run_fixture('dependencies { implementation(project(":a")) }', doc("## 2. Contracts\n"))
        self.assertEqual(rep.count(ca.ERROR), 0, rep.findings)

    def test_forbidden_edge_detected(self):
        # :a -> :b exists only in the PROPOSED block, so the ignored block must not allow it
        rep = self.run_fixture('dependencies { implementation(project(":a")) }', doc("## 2. Contracts\n"))
        with tempfile.TemporaryDirectory() as d:
            make_fixture(Path(d), 'dependencies { implementation(project(":a")) }', doc("## 2. Contracts\n"))
            (Path(d) / "a/build.gradle.kts").write_text('dependencies { implementation(project(":b")) }')
            rep = ca.run(Path(d))[0]
        edge_errors = self.msgs(rep, "edges")
        self.assertTrue(any(":a -> :b not allowed" in m for m in edge_errors), edge_errors)
        self.assertTrue(any("cycle" in m for m in edge_errors), edge_errors)

    def test_context_map_drift_detected(self):
        rep = self.run_fixture("", doc("## 2. Contracts\n"))  # build has no edge, map says :a
        self.assertTrue(any("depends_on != build file" in m for m in self.msgs(rep, "edges")))

    def test_missing_heading_detected(self):
        rep = self.run_fixture('dependencies { implementation(project(":a")) }', doc())
        self.assertTrue(any("'## 2. Contracts'" in m for m in self.msgs(rep, "doc-headings")))

    def test_missing_doc_is_error(self):
        with tempfile.TemporaryDirectory() as d:
            make_fixture(Path(d), 'dependencies { implementation(project(":a")) }', doc("## 2. Contracts\n"))
            (Path(d) / "architecture/modules/b.md").unlink()
            rep = ca.run(Path(d))[0]
        self.assertTrue(any("doc file missing" in m for m in self.msgs(rep, "doc-exists")))

    def test_headings_inside_comments_ignored(self):
        with tempfile.TemporaryDirectory() as d:
            p = Path(d) / "T.md"
            p.write_text("<!-- ## 9. Hidden -->\n## 1. Real\n```\n## 8. Fenced\n```\n")
            self.assertEqual(ca.required_headings(p), [("1", "Real")])


if __name__ == "__main__":
    unittest.main()
