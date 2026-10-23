package fantasy.gradle

import fantasy.gradle.testing.readTestReport
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class TestReportTest {
    @get:Rule val temporary = TemporaryFolder()
    private val expected = setOf("recipes", "attributes")

    private fun report(xml: String) = temporary.newFile().apply { writeText(xml) }
    private fun cases(vararg names: String) = "<testsuite>" + names.joinToString("") { "<testcase name=\"$it\"/>" } + "</testsuite>"

    @Test fun `complete successful report passes regardless of order`() {
        val result = readTestReport(report(cases("attributes", "recipes")), expected)
        assertTrue(result.successful)
        assertEquals("PASS — 2/2", result.summary())
        assertTrue(result.summary(serverFailed = true).startsWith("FAIL — 2/2"))
    }

    @Test fun `failure error and skipped each fail verification and summary`() {
        for (tag in listOf("failure", "error", "skipped")) {
            val result = readTestReport(report("""
                <testsuite>
                    <testcase name="recipes"><$tag message="broken"/></testcase>
                    <testcase name="attributes"/>
                </testsuite>
            """.trimIndent()), expected)
            assertFalse(result.successful)
            assertTrue(result.summary().startsWith("FAIL — 1/2"))
            assertTrue(result.summary().contains("recipes: broken"))
        }
    }

    @Test fun `missing unexpected duplicated and empty test lists fail`() {
        for (names in listOf(listOf("recipes"), listOf("recipes", "other"), listOf("recipes", "recipes"),
                listOf("recipes", "attributes", "attributes"), emptyList())) {
            val result = readTestReport(report(cases(*names.toTypedArray())), expected)
            assertFalse(result.successful)
            assertTrue(result.summary().contains("Report does not match"))
        }
    }

    @Test fun `missing and malformed reports fail`() {
        val missing = temporary.root.resolve("missing.xml")
        assertFalse(readTestReport(missing, expected).successful)
        assertFalse(readTestReport(report("<testsuite>"), expected).successful)
    }

    @Test fun `suite failures cannot be hidden by successful cases`() {
        val xml = cases("recipes", "attributes").replace("</testsuite>", "<error message=\"suite failed\"/></testsuite>")
        assertFalse(readTestReport(report(xml), expected).successful)
    }
}
