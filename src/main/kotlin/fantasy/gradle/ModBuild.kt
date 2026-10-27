package fantasy.gradle

import fantasy.gradle.minecraft.MinecraftVersions
import fantasy.gradle.resources.packMetadata
import fantasy.gradle.resources.json
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
    templatesDirectory: org.gradle.api.file.Directory = modTemplates(target),
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
    if (target.loader != Loader.FABRIC) extensions.configure<ResourcesExtension> {
        generate("generatePackMetadata") {
            contents.set(mapOf("pack.mcmeta" to MinecraftVersions[target.minecraft].packMetadata(metadata.name)))
            outputDirectory.set(layout.buildDirectory.dir("generated/resources/pack"))
        }
    }
    if ((target.settings as? ForgeSettings)?.metadata == ForgeMetadata.MCMOD_INFO) {
        extensions.configure<ResourcesExtension> {
            generate("generateModMetadata") {
                contents.set(mapOf("mcmod.info" to json(listOf(mapOf(
                    "modid" to metadata.id, "name" to metadata.name, "version" to target.modVersion,
                    "mcversion" to target.minecraft, "description" to metadata.description,
                    "url" to metadata.homepage, "authorList" to metadata.authors.split(',').map(String::trim),
                    "logoFile" to metadata.icon, "useDependencyInformation" to true,
                    "requiredMods" to listOf("forge@${target.loaderDependencyRange}") + target.dependencies.map { "${it.id}@${it.versionRange(target.loader)}" },
                    "dependencies" to listOf("forge@${target.loaderDependencyRange}") + target.dependencies.map { "${it.id}@${it.versionRange(target.loader)}" },
                )))))
            }
        }
    } else if (target.loader == Loader.FABRIC) {
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
            from(templatesDirectory) { exclude("pack.mcmeta") }
            into(layout.buildDirectory.dir("generated/resources/metadata"))
            expand(values)
        }
        extensions.configure<ResourcesExtension> { sources.from(templates) }
    }
}

private fun Project.modTemplates(target: Target): org.gradle.api.file.Directory {
    val loader = rootProject.layout.projectDirectory.dir("loaders/${target.loader.id}")
    val platform = layout.projectDirectory.dir("src/main/templates")
    return platform.takeIf { it.file(target.settings.metadataFile).asFile.isFile } ?: loader.dir("src/main/templates")
}
