package fantasy.gradle

import com.google.gson.Gson
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*

fun Project.generateModConstants(packageName: String, values: Map<String, String>) {
    require(packageName.split('.').all { it.matches(Regex("[a-z][a-z0-9]*")) }) { "Invalid Java package: $packageName" }
    require(values.isNotEmpty() && values.keys.all { it.matches(Regex("[A-Z][A-Z0-9_]*")) }) { "Invalid constants" }
    val constants = tasks.register<GenerateFiles>("generateModConstants") {
        val source = buildString {
            appendLine("package $packageName;")
            appendLine("public final class ModConstants {")
            values.forEach { (name, value) -> appendLine("    public static final String $name = ${Gson().toJson(value)};") }
            appendLine("    private ModConstants() {}")
            appendLine("}")
        }
        contents.set(mapOf("${packageName.replace('.', '/')}/ModConstants.java" to source))
        outputDirectory.set(layout.buildDirectory.dir("generated/sources/constants"))
    }
    extensions.configure<SourceSetContainer> { named("main") { java.srcDir(constants.flatMap { it.outputDirectory }) } }
    artifacts.add("sharedSources", constants.flatMap { it.outputDirectory }) { type = "directory" }
}
