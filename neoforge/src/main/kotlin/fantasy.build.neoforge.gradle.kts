import fantasy.gradle.testing.*
import fantasy.gradle.minecraft.MinecraftVersions
import fantasy.gradle.*
import fantasy.gradle.Target
import org.slf4j.event.Level

plugins {
    id("net.neoforged.moddev")
    `java-library`
    `maven-publish`
    idea
}

val target = extensions.getByType<Target>()
val metadata = extensions.getByType<ModMetadata>()
require(target.loader == Loader.NEOFORGE) { "The NeoForge adapter requires a NeoForge target" }
MinecraftVersions[target.minecraft].loader(Loader.NEOFORGE)
configureLibraryRepositories(target)

neoForge {
    version = target.loaderVersion
    runs {
        register("client") {
            client()
            systemProperty("neoforge.enabledGameTestNamespaces", metadata.id)
        }
        register("server") {
            server()
            programArgument("--nogui")
            systemProperty("neoforge.enabledGameTestNamespaces", metadata.id)
        }
        register("gameTestServer") {
            type = "gameTestServer"
            systemProperty("neoforge.enabledGameTestNamespaces", metadata.id)
        }
        if (target.build.dataGeneration) register("data") {
            data()
            programArguments.addAll(
                "--mod", metadata.id, "--all", "--output",
                layout.buildDirectory.dir("generated/resources/datagen").get().asFile.absolutePath,
                "--existing", file("src/main/resources").absolutePath,
            )
        }
        configureEach {
            systemProperty("forge.logging.markers", "REGISTRIES")
            logLevel = Level.DEBUG
        }
    }
    mods.register(metadata.id) { sourceSet(sourceSets.main.get()) }
}
if (target.build.dataGeneration) sourceSets.main {
    resources.srcDir(layout.buildDirectory.dir("generated/resources/datagen"))
}
dependencies {
    target.dependencies.filterIsInstance<GeckoLib>().forEach { implementation(it.notation(target)) }
}
neoForge.ideSyncTask(tasks.named("generateModMetadata"))
idea.module {
    isDownloadSources = true
    isDownloadJavadoc = true
}
publishing {
    publications.register<MavenPublication>("mavenJava") { from(components["java"]) }
    repositories.maven { url = uri(layout.projectDirectory.dir("repo")) }
}
registerReleaseExport(tasks.jar.flatMap { it.archiveFile })

extensions.add("fantasyTestRuntime", TestRuntime { suite, tests, report ->
    neoForge {
        mods.register(suite.module.id) { sourceSet(tests) }
        runs.named("gameTestServer") {
            logLevel = Level.INFO
            sourceSet.set(tests)
            gameDirectory.set(layout.buildDirectory.dir("run/test"))
            systemProperty("neoforge.enabledGameTestNamespaces", suite.module.id)
            systemProperty(suite.module.reportProperty, report.get().asFile.absolutePath)
        }
        runs.matching { it.name != "gameTestServer" }.configureEach {
            loadedMods.set(setOf(mods.getByName(suite.modId)))
        }
    }
})
