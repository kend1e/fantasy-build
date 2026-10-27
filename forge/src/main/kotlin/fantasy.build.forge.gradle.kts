import fantasy.gradle.minecraft.MinecraftVersions
import fantasy.gradle.testing.*
import fantasy.gradle.*
import fantasy.gradle.Target
import fantasy.gradle.forge.ForgeRuntimeProfile

plugins {
    id("net.minecraftforge.gradle")
    java
    idea
    eclipse
    `maven-publish`
}

val target = extensions.getByType<Target>()
val metadata = extensions.getByType<ModMetadata>()
require(target.loader == Loader.FORGE) { "The Forge adapter requires a Forge target" }
private val profile = ForgeRuntimeProfile.forTarget(target)
configureLibraryRepositories(target)

minecraft {
    if (MinecraftVersions[target.minecraft].obfuscated) {
        val forge = MinecraftVersions[target.minecraft].forge
        mappings(forge.mappingsChannel, forge.mappingsVersion ?: target.minecraft)
    }
    runs {
        configureEach {
            workingDir.set(layout.projectDirectory.dir("run"))
            jvmArgs(MinecraftVersions[target.minecraft].forge.launchJvmArgs)
            if (profile.classicLaunch) {
                systemProperty("forge.logging.markers", "REGISTRIES")
                systemProperty("forge.logging.console.level", "debug")
                systemProperty("forge.enabledGameTestNamespaces", metadata.id)
            }
        }
        register("client")
        register("server") { args("--nogui") }
        if (MinecraftVersions[target.minecraft].forge.tests != null) register("gameTestServer")
        if (target.build.dataGeneration) register("data") {
            workingDir.set(layout.projectDirectory.dir("run-data"))
            args("--mod", metadata.id, "--all", "--output",
                layout.buildDirectory.dir("generated/resources/datagen").get(),
                "--existing", layout.projectDirectory.dir("src/main/resources"))
        }
    }
}
repositories {
    minecraft.mavenizer(this)
    maven(fg.forgeMaven)
    maven(fg.minecraftLibsMaven)
    mavenCentral()
}
dependencies {
    // FG 7's detached launch configuration needs explicit variant attributes.
    val forge = create("net.minecraftforge:forge:${target.minecraft}-${target.loaderVersion}") as ExternalModuleDependency
    forge.attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        if (MinecraftVersions[target.minecraft].obfuscated) {
            attribute(Attribute.of("net.minecraftforge.mappings.channel", String::class.java), MinecraftVersions[target.minecraft].forge.mappingsChannel)
            attribute(Attribute.of("net.minecraftforge.mappings.version", String::class.java), MinecraftVersions[target.minecraft].forge.mappingsVersion ?: target.minecraft)
        }
    }
    implementation(minecraft.dependency(project.name, forge))
    if (profile == ForgeRuntimeProfile.OFFICIAL) {
        implementation("net.sf.jopt-simple:jopt-simple:5.0.4") { version { strictly("5.0.4") } }
    }
    if (target.build.forgeNames == ForgeNames.OFFICIAL) {
        target.dependencies.filterIsInstance<GeckoLib>().forEach { implementation(it.notation(target)) }
    }
}
// Mavenizer publishes the old Forge launcher as a self-dependency without variant attributes.
dependencies.components.withModule("net.minecraftforge:forge") {
    if (id.version == "${target.minecraft}-${target.loaderVersion}") allVariants {
        withDependencies {
            filter { it.group == "net.minecraftforge" && it.name == "forge" }.forEach { dependency ->
                dependency.attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
                    attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
                    if (MinecraftVersions[target.minecraft].obfuscated) {
                        val forge = MinecraftVersions[target.minecraft].forge
                        attribute(Attribute.of("net.minecraftforge.mappings.channel", String::class.java), forge.mappingsChannel)
                        attribute(Attribute.of("net.minecraftforge.mappings.version", String::class.java), forge.mappingsVersion ?: target.minecraft)
                    }
                }
            }
        }
    }
}

if (profile.classicLaunch) tasks.jar {
    manifest.attributes(
        "Specification-Title" to metadata.id,
        "Specification-Vendor" to metadata.authors,
        "Specification-Version" to "1",
        "Implementation-Title" to project.name,
        "Implementation-Version" to archiveVersion,
        "Implementation-Vendor" to metadata.authors,
    )
}
if (target.build.dataGeneration) sourceSets.main {
    resources.srcDir(layout.buildDirectory.dir("generated/resources/datagen"))
}
if (target.build.forgeNames == ForgeNames.SRG) {
    pluginManager.apply("fantasy.build.forge-srg")
} else {
    registerReleaseExport(tasks.jar.flatMap { it.archiveFile })
}
idea.module {
    isDownloadSources = true
    isDownloadJavadoc = true
}

extensions.add("fantasyTestRuntime", TestRuntime { suite, tests, report ->
    minecraft.runs.named("gameTestServer") {
        with(tests) {
            if (MinecraftVersions[target.minecraft].loader(Loader.FORGE).tests == TestRegistration.FORGE_CONTAINER) {
                mods.register(suite.modId) { source(sourceSets.main.get()) }
                mods.register(suite.module.id) { source(tests) }
            }
            systemProperty("forge.enabledGameTestNamespaces", suite.module.id)
            systemProperty("forge.logging.console.level", "info")
            systemProperty(suite.module.reportProperty, report.get().asFile.absolutePath)
            workingDir.set(layout.buildDirectory.dir("run/test"))
        }
    }
})
