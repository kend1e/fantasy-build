package fantasy.gradle

import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

fun Project.configureModSources(coreProject: String = ":core") {
    val target = extensions.getByType<Target>()
    val root = rootProject.layout.projectDirectory
    extensions.configure<PlatformExtension> {
        sources.from(root.dir("minecraft/common/src/main/java"))
        sources.from(root.dir("minecraft/${target.minecraft}/src/main/java"))
        sources.from(root.dir("loaders/${target.loader.id}/src/main/java"))
        sources.from(root.dir("loaders/${target.loader.id}/${target.minecraft}/src/main/java"))
    }
    for (kind in listOf("Sources", "Resources")) {
        dependencies.add("shared${kind}Dependencies", dependencies.project(mapOf("path" to coreProject, "configuration" to "shared$kind")))
    }
    tasks.named("generateResources") { dependsOn(configurations.named("sharedResourceFiles")) }
    tasks.matching { it.name == "runClient" || it.name == "runServer" }.configureEach { dependsOn(tasks.named("classes")) }
}
