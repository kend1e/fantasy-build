package fantasy.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.*

class ResourcesPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        val resourceExtension = extensions.create<ResourcesExtension>("fantasyResources")
        val generated = objects.fileCollection()
        tasks.register("generateResources") {
            group = "build"
            description = "Prepares the declared resources."
            dependsOn(resourceExtension.sources, generated)
        }
        resourceExtension.generators.all {
            val generation = this
            generation.outputDirectory.convention(layout.buildDirectory.dir("generated/resources/${generation.name}"))
            val task = tasks.register<GenerateFiles>(generation.name) {
                contents.set(generation.contents)
                outputDirectory.set(generation.outputDirectory)
            }
            generated.from(task.flatMap { it.outputDirectory })
        }
        pluginManager.withPlugin("java") {
            extensions.getByType<SourceSetContainer>().named("main") {
                resources.srcDir(resourceExtension.sources)
                resources.srcDir(generated)
            }
        }
    }
}
