package fantasy.gradle

import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.*

enum class ForgeNames { OFFICIAL, SRG }

data class LoaderBuild(
    val dataGeneration: Boolean = false,
    val forgeNames: ForgeNames = ForgeNames.OFFICIAL,
) : java.io.Serializable

fun Project.registerReleaseExport(artifactFile: Provider<RegularFile>) {
    val target = extensions.getByType<Target>()
    val metadata = extensions.getByType<ModMetadata>()
    val releaseFile = rootProject.layout.projectDirectory.file("dist/${target.releaseFileName(metadata)}")
    val export = tasks.register<ExportRelease>("exportRelease") {
        group = "build"
        description = "Copies the installable ${target.name} artifact into root dist."
        artifact.set(artifactFile)
        destination.set(releaseFile)
    }
    tasks.named("assemble") { dependsOn(export) }
}

fun Project.configureLibraryRepositories(target: Target) {
    if (target.dependencies.any { it is GeckoLib }) repositories.maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") {
        name = "GeckoLib"
        content {
            includeGroupByRegex("software\\.bernie.*")
            includeGroup("com.eliotlash.mclib")
            includeGroup("com.geckolib")
        }
    }
    if (target.dependencies.any { it is ClothConfig || it is AutoConfig1u }) {
        repositories.maven("https://maven.shedaniel.me/")
        repositories.maven("https://maven.terraformersmc.com/releases/")
    }
}
