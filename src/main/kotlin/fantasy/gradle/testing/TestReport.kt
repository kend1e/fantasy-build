package fantasy.gradle.testing

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.w3c.dom.Element

internal data class TestReportResult(
    val passedCount: Int,
    val expectedCount: Int,
    val problems: List<String>,
) {
    val successful: Boolean get() = problems.isEmpty()

    fun summary(serverFailed: Boolean = false): String = buildString {
        val status = if (successful && !serverFailed) "PASS" else "FAIL"
        append("$status — $passedCount/$expectedCount")
        problems.forEach { append("\n  $it") }
        if (serverFailed) append("\n  Server task failed; see the task output")
    }
}

internal fun readTestReport(file: File, expected: Set<String>): TestReportResult {
    fun invalid(problem: String) = TestReportResult(0, expected.size, listOf(problem))
    if (!file.isFile) return invalid("No report; see the task output")

    return try {
        val factory = DocumentBuilderFactory.newInstance()
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true)
        val document = factory.newDocumentBuilder().parse(file)
        val nodes = document.getElementsByTagName("testcase")
        val cases = (0 until nodes.length).map { nodes.item(it) as Element }
        val names = cases.map { it.getAttribute("name") }
        val issueTags = listOf("failure", "error", "skipped")
        val unsuccessful = cases.filter { case -> issueTags.any { case.getElementsByTagName(it).length > 0 } }
        val problems = buildList {
            unsuccessful.forEach { case ->
                val issue = issueTags.firstNotNullOf { case.getElementsByTagName(it).item(0) as? Element }
                add("${case.getAttribute("name")}: ${issue.getAttribute("message")}")
            }
            if (names.size != expected.size || names.toSet() != expected) {
                val duplicates = names.groupingBy { it }.eachCount().filterValues { it > 1 }.keys
                add("Report does not match the expected test list: missing=${expected - names.toSet()}, unexpected=${names.toSet() - expected}, duplicates=$duplicates")
            }
            if (unsuccessful.isEmpty() && issueTags.any { document.getElementsByTagName(it).length > 0 }) {
                add("Report contains failures, errors or skipped tests")
            }
        }
        TestReportResult(cases.size - unsuccessful.size, expected.size, problems)
    } catch (_: Exception) {
        invalid("Cannot read report; see $file")
    }
}
