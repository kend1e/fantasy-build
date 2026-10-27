package fantasy.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

fun Project.configureModSources(sourceDirectories: List<String>, coreProject: String = ":core") {
    val root = rootProject.layout.projectDirectory
    extensions.configure<PlatformExtension> {
        sources.from(sourceDirectories.map(root::dir))
    }
    for (kind in listOf("Sources", "Resources")) {
        dependencies.add("shared${kind}Dependencies", dependencies.project(mapOf("path" to coreProject, "configuration" to "shared$kind")))
    }
    tasks.named("generateResources") { dependsOn(configurations.named("sharedResourceFiles")) }
    tasks.matching { it.name == "runClient" || it.name == "runServer" }.configureEach { dependsOn(tasks.named("classes")) }
}
