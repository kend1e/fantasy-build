package fantasy.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

class SharedPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        pluginManager.apply("java-library")
        pluginManager.apply("fantasy.build.platform")
        pluginManager.apply("fantasy.build.resources")
        val sources = configurations.consumable("sharedSources")
        val resources = configurations.consumable("sharedResources")
        artifacts.add(sources.name, layout.projectDirectory.dir("src/main/java"))
        artifacts.add(resources.name, tasks.named("processResources")) { type = "directory" }
    }
}
