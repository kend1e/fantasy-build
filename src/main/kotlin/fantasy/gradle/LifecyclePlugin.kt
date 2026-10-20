package fantasy.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.kotlin.dsl.*

class LifecyclePlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        require(this == rootProject) { "Apply fantasy.build.lifecycle to the root project" }
        pluginManager.apply("base")
        val modules = subprojects.filter { it.path == ":core" || it.path.startsWith(":platforms:") }
        for (name in listOf("assemble", "check", "clean")) {
            tasks.named(name) { dependsOn(modules.map { "${it.path}:$name" }) }
        }
        tasks.named<Delete>("clean") { delete(layout.projectDirectory.dir("dist")) }
        tasks.register("generateResources") {
            group = "build"
            description = "Generates resources on every declared target."
            dependsOn(modules.map { "${it.path}:generateResources" })
        }
        tasks.register("test") {
            group = "verification"
            description = "Runs unit tests and Minecraft server tests on every declared target."
            dependsOn(modules.map { "${it.path}:test" })
        }
        gradle.includedBuilds.find { it.projectDir.name == "build-logic" }?.let { build ->
            tasks.named("check") { dependsOn(build.task(":check")) }
        }
    }
}
