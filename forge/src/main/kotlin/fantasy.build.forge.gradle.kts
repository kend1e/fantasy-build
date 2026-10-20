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
    mappings("official", target.minecraft)
    runs {
        configureEach {
            workingDir.set(layout.projectDirectory.dir("run"))
            if (profile.classicLaunch) {
                systemProperty("forge.logging.markers", "REGISTRIES")
                systemProperty("forge.logging.console.level", "debug")
                systemProperty("forge.enabledGameTestNamespaces", metadata.id)
            }
        }
        register("client")
        register("server") { args("--nogui") }
        register("gameTestServer")
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
        attribute(Attribute.of("net.minecraftforge.mappings.channel", String::class.java), "official")
        attribute(Attribute.of("net.minecraftforge.mappings.version", String::class.java), target.minecraft)
    }
    implementation(minecraft.dependency(project.name, forge))
    if (profile == ForgeRuntimeProfile.OFFICIAL) {
        implementation("net.sf.jopt-simple:jopt-simple:5.0.4") { version { strictly("5.0.4") } }
    }
    if (target.build.forgeNames == ForgeNames.OFFICIAL) {
        target.dependencies.filterIsInstance<GeckoLib>().forEach { implementation(it.notation(target)) }
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
