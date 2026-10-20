package fantasy.gradle

import org.gradle.api.Project
import org.gradle.api.initialization.Settings
import org.gradle.api.tasks.Sync
import org.gradle.kotlin.dsl.*

fun Settings.includeModTargets(targets: List<Target>) {
    require(targets.map { it.path }.distinct().size == targets.size) { "Duplicate build targets" }
    targets.forEach { include(it.path) }
}

fun Project.configureMod(
    metadata: ModMetadata,
    target: Target,
    templatesDirectory: org.gradle.api.file.Directory = layout.projectDirectory.dir("src/main/templates"),
) {
    metadata.validate(target)
    pluginManager.apply("fantasy.build.platform")
    pluginManager.apply("fantasy.build.resources")
    group = metadata.group
    version = target.modVersion
    extensions.add(ModMetadata::class.java, "modMetadata", metadata)
    extensions.add(Target::class.java, "platformTarget", target)
    extensions.configure<PlatformExtension> {
        javaVersion.set(target.java)
        archiveName.set(metadata.id)
    }
    if (target.loader == Loader.FABRIC) {
        val template = providers.fileContents(templatesDirectory.file("fabric.mod.json")).asText
        extensions.configure<ResourcesExtension> {
            generate("generateModMetadata") {
                contents.set(template.map { mapOf("fabric.mod.json" to renderFabricMetadata(it, metadata, target)) })
                outputDirectory.set(layout.buildDirectory.dir("generated/resources/metadata"))
            }
        }
    } else {
        val values = metadata.templateValues(target)
        val templates = tasks.register<Sync>("generateModMetadata") {
            inputs.properties(values)
            from(templatesDirectory)
            into(layout.buildDirectory.dir("generated/resources/metadata"))
            expand(values)
        }
        extensions.configure<ResourcesExtension> { sources.from(templates) }
    }
}
