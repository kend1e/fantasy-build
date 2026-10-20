package fantasy.gradle

import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*

fun Project.configureModResources(directory: Directory = rootProject.layout.projectDirectory.dir("resources")) {
    val target = extensions.getByType<Target>()
    val metadata = extensions.getByType<ModMetadata>()
    val geckolib = target.dependencies.filterIsInstance<GeckoLib>().singleOrNull()
    extensions.configure<ResourcesExtension> {
        if (geckolib == null) {
            sources.from(directory)
        } else {
            val prepared = tasks.register<Sync>("prepareSharedResources") {
                from(directory) { exclude("assets/${metadata.id}/geo/**") }
                from(directory.dir("assets/${metadata.id}/geo")) {
                    into("assets/${metadata.id}/${geckolib.modelDirectory}")
                }
                into(layout.buildDirectory.dir("generated/resources/shared"))
                duplicatesStrategy = DuplicatesStrategy.FAIL
            }
            sources.from(prepared)
        }
    }
}
