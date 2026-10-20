import fantasy.gradle.testing.*
import fantasy.gradle.minecraft.MinecraftVersions
import fantasy.gradle.*
import fantasy.gradle.Target
import org.gradle.api.tasks.bundling.AbstractArchiveTask

plugins {
    id("fabric-loom")
    `maven-publish`
}

val target = extensions.getByType<Target>()
require(target.loader == Loader.FABRIC) { "The Fabric adapter requires a Fabric target" }
MinecraftVersions[target.minecraft].loader(Loader.FABRIC)
configureLibraryRepositories(target)

dependencies {
    minecraft("com.mojang:minecraft:${target.minecraft}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${target.loaderVersion}")
    target.dependencies.forEach { library ->
        when (library) {
            is FabricApi -> modImplementation(library.notation)
            is GeckoLib -> modImplementation(library.notation(target))
            is ClothConfig -> modApi(library.notation) { exclude(group = "net.fabricmc.fabric-api") }
        }
    }
}

if (target.build.dataGeneration) {
    require(target.dependencies.any { it is FabricApi }) { "${target.name}: Fabric data generation requires Fabric API" }
    fabricApi.configureDataGeneration {
        addToResources.set(false)
        outputDirectory.set(layout.buildDirectory.file("generated/resources/datagen"))
    }
    // Loom 1.13.6 otherwise resolves directories eagerly and loses task dependencies.
    sourceSets.main { resources.srcDir(layout.buildDirectory.dir("generated/resources/datagen")) }
}

java.withSourcesJar()
val archiveName = base.archivesName
tasks.jar {
    val licenseName = "LICENSE.md_${archiveName.get()}"
    inputs.property("archivesName", archiveName)
    from(rootProject.layout.projectDirectory.file("LICENSE.md")) { rename { licenseName } }
}
publishing.publications.create<MavenPublication>("mavenJava") {
    artifactId = archiveName.get()
    from(components["java"])
}
registerReleaseExport(tasks.named<AbstractArchiveTask>("remapJar").flatMap { it.archiveFile })

extensions.add("fantasyTestRuntime", TestRuntime { suite, tests, report ->
    fabricApi.configureTests {
        createSourceSet.set(false)
        enableClientGameTests.set(false)
    }
    loom.mods {
        register(suite.modId) { sourceSet(sourceSets.main.get()) }
        register(suite.module.id) { sourceSet(tests) }
    }
    loom.runs.named("gameTest") {
        source(tests)
        runDir("build/run/test")
        property("fabric-api.gametest.report-file", report.get().asFile.absolutePath)
    }
})
