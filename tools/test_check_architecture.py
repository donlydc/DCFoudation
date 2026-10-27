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


RULES_WITH_TEST = RULES + "## Test edges\n```text\n# test-only\n:b -> :a\n```\n"


class BoundaryOnlyTest(unittest.TestCase):
    """KD-02 / MP:R2.1: --boundaries-only must fail on any forbidden, unlisted or unparseable edge."""

    def boundaries(self, a_build="", b_build="", rules=RULES, extra=None):
        with tempfile.TemporaryDirectory() as d:
            root = Path(d)
            make_fixture(root, b_build, doc())
            (root / "a/build.gradle.kts").write_text(a_build)
            (root / "architecture/dependency-rules.md").write_text(rules)
            for rel, text in (extra or {}).items():
                (root / rel).write_text(text)
            rep, _ = ca.run_boundaries(root)
            code = ca.main(["--root", d, "--boundaries-only"])
        return rep, code

    def errors(self, rep):
        return [f["message"] for f in rep.findings if f["severity"] == ca.ERROR]

    def test_allowed_edges_pass_without_yaml_or_docs(self):
        rep, code = self.boundaries(b_build='dependencies { implementation(project(":a")) }')
        self.assertEqual(self.errors(rep), [])
        self.assertEqual(code, 0)

    def test_forbidden_edge_fails_in_every_production_form(self):
        # :a -> :b is only in the PROPOSED block, so it is forbidden
        for decl in ('implementation(project(":b"))', 'api(project(":b"))', 'compileOnly(project(":b"))',
                     'ksp(project(":b"))', '"implementation"(project(":b"))', 'api(project(path = ":b"))',
                     'add("implementation", project(":b"))', 'debugImplementation(project(":b"))'):
            with self.subTest(decl=decl):
                rep, code = self.boundaries(a_build="dependencies { %s }" % decl,
                                            b_build='dependencies { implementation(project(":a")) }')
                self.assertTrue(any(":a -> :b not allowed" in m for m in self.errors(rep)), self.errors(rep))
                self.assertEqual(code, 1)

    def test_unlisted_test_edge_fails_and_listed_test_edge_passes(self):
        a = 'dependencies { implementation(project(":a")) }'
        b_test = 'dependencies { %s\n testImplementation(project(":a")) }'
        for decl in ("testImplementation", "androidTestImplementation", "kspTest", "testFixturesApi"):
            with self.subTest(decl=decl):
                bld = 'dependencies { implementation(project(":a"))\n %s(project(":a")) }' % decl
                rep, code = self.boundaries(b_build=bld)  # RULES has no test-only block
                self.assertTrue(any("test edge :b -> :a not allowed" in m for m in self.errors(rep)), self.errors(rep))
                self.assertEqual(code, 1)
                rep, code = self.boundaries(b_build=bld, rules=RULES_WITH_TEST)
                self.assertEqual(self.errors(rep), [])
                self.assertEqual(code, 0)

    def test_unparseable_forms_fail_closed(self):
        for decl in ('implementation(projects.b)', 'testImplementation(platform(project(":a")))'):
            with self.subTest(decl=decl):
                rep, code = self.boundaries(b_build='dependencies { implementation(project(":a"))\n %s }' % decl)
                self.assertTrue(any("unparseable" in m for m in self.errors(rep)), self.errors(rep))
                self.assertEqual(code, 1)

    def test_commented_edge_is_ignored(self):
        rep, code = self.boundaries(a_build='// implementation(project(":b"))\n/* api(project(":b")) */',
                                    b_build='dependencies { implementation(project(":a")) }')
        self.assertEqual(self.errors(rep), [])
        self.assertEqual(code, 0)

    def test_string_literals_cannot_hide_edges_from_comment_stripping(self):
        edge = 'implementation(project(":b"))'  # :a -> :b is forbidden
        b = 'dependencies { implementation(project(":a")) }'
        cases = {
            # (a) `/*` inside one string and `*/` inside a later one must not delete the edge between them
            "block-in-strings": 'val e = "/META-INF/*.kotlin_module"\ndependencies { %s }\nval x = "**/x"\n' % edge,
            # (b) `//` inside a string must not swallow the rest of the line
            "slashes-in-string": 'val u = "a//b"; dependencies { %s }\n' % edge,
            # (c) `/*` inside a line comment must not open a block comment
            "block-in-line-comment": '// src/*/java\ndependencies { %s }\nval y = "*/"\n' % edge,
            "raw-string": 'val r = """ /* \n // """\ndependencies { %s }\nval z = "*/"\n' % edge,
            "char-quote": "val q = '\"'\ndependencies { %s }\n" % edge,
        }
        for name, a_build in cases.items():
            with self.subTest(case=name):
                rep, code = self.boundaries(a_build=a_build, b_build=b)
                self.assertTrue(any(":a -> :b not allowed" in m for m in self.errors(rep)), self.errors(rep))
                self.assertEqual(code, 1)

    def test_real_comments_are_still_stripped(self):
        a_build = ('// implementation(project(":b"))\n/* nested /* api(project(":b")) */ implementation(project(":b")) */\n'
                   'val s = "keep // this"\n')
        rep, code = self.boundaries(a_build=a_build, b_build='dependencies { implementation(project(":a")) }')
        self.assertEqual(self.errors(rep), [])
        self.assertEqual(code, 0)

    def test_app_unknown_target_cycle_and_testing_fail(self):
        a = 'dependencies { implementation(project(":b")) }'
        rep, _ = self.boundaries(a_build=a, b_build='dependencies { implementation(project(":a")) }',
                                 rules=RULES.replace("## PROPOSED", "## Other\n```text\n:a -> :b\n```\n## PROPOSED"))
        self.assertTrue(any("cycle" in m for m in self.errors(rep)), self.errors(rep))
        rep, _ = self.boundaries(b_build='dependencies { implementation(project(":a"))\n implementation(project(":app")) }')
        self.assertTrue(any(":app is forbidden" in m for m in self.errors(rep)), self.errors(rep))
        self.assertTrue(any(":app: target is not a Gradle module" in m for m in self.errors(rep)), self.errors(rep))
        rep, _ = self.boundaries(b_build='dependencies { implementation(project(":a"))\n implementation(project(":testing")) }')
        self.assertTrue(any(":testing in a production configuration" in m for m in self.errors(rep)), self.errors(rep))

    def test_root_build_script_edges_fail(self):
        rep, code = self.boundaries(b_build='dependencies { implementation(project(":a")) }',
                                    extra={"build.gradle.kts": 'subprojects { dependencies { implementation(project(":a")) } }'})
        self.assertTrue(any("root build script declares a project edge" in m for m in self.errors(rep)), self.errors(rep))
        self.assertEqual(code, 1)

    def test_missing_rules_block_fails(self):
        rep, code = self.boundaries(rules="# nothing machine-checkable\n")
        self.assertTrue(any("no machine-checkable" in m for m in self.errors(rep)))
        self.assertEqual(code, 1)


if __name__ == "__main__":
    unittest.main()
