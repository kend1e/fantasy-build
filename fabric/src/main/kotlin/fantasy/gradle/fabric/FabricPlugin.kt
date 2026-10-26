package fantasy.gradle.fabric

import fantasy.gradle.*
import fantasy.gradle.Target
import fantasy.gradle.minecraft.MinecraftVersions
import fantasy.gradle.testing.*
import net.fabricmc.loom.api.LoomGradleExtensionAPI
import net.fabricmc.loom.api.fabricapi.FabricApiExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*

class FabricPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        val target = extensions.getByType<Target>()
        require(target.loader == Loader.FABRIC) { "The Fabric adapter requires a Fabric target" }
        val version = MinecraftVersions[target.minecraft]
        version.loader(Loader.FABRIC)
        pluginManager.apply(if (version.obfuscated) "net.fabricmc.fabric-loom-remap" else "net.fabricmc.fabric-loom")
        pluginManager.apply("maven-publish")
        val loom = extensions.getByType<LoomGradleExtensionAPI>()
        val fabricApi = extensions.getByType<FabricApiExtension>()
        val sourceSets = extensions.getByType<SourceSetContainer>()
        configureLibraryRepositories(target)

        dependencies {
            add("minecraft", "com.mojang:minecraft:${target.minecraft}")
            if (version.obfuscated) add("mappings", loom.officialMojangMappings())
            add(if (version.obfuscated) "modImplementation" else "implementation", "net.fabricmc:fabric-loader:${target.loaderVersion}")
            target.dependencies.forEach { library ->
                when (library) {
                    is FabricApi -> add(if (version.obfuscated) "modImplementation" else "implementation", library.notation)
                    is GeckoLib -> add(if (version.obfuscated) "modImplementation" else "implementation", library.notation(target)) {
                        (this as ExternalModuleDependency).exclude(mapOf("group" to "net.fabricmc", "module" to "fabric-loader"))
                        if (target.dependencies.any { it is FabricApi }) {
                            exclude(mapOf("group" to "net.fabricmc.fabric-api"))
                        }
                    }
                    is ClothConfig -> add(if (version.obfuscated) "modApi" else "api", library.notation) {
                        (this as ExternalModuleDependency).exclude(mapOf("group" to "net.fabricmc.fabric-api"))
                        exclude(mapOf("group" to "net.fabricmc", "module" to "fabric-loader"))
                    }
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
            sourceSets.named("main") { resources.srcDir(layout.buildDirectory.dir("generated/resources/datagen")) }
        }

        extensions.getByType<JavaPluginExtension>().withSourcesJar()
        val archiveName = extensions.getByType<BasePluginExtension>().archivesName
        tasks.named<Jar>("jar") {
            val licenseName = "LICENSE.md_${archiveName.get()}"
            inputs.property("archivesName", archiveName)
            from(rootProject.layout.projectDirectory.file("LICENSE.md")) { rename { licenseName } }
        }
        extensions.getByType<PublishingExtension>().publications.create<MavenPublication>("mavenJava") {
            artifactId = archiveName.get()
            from(components["java"])
        }
        registerReleaseExport(tasks.named<AbstractArchiveTask>(if (version.obfuscated) "remapJar" else "jar").flatMap { it.archiveFile })

        extensions.add("fantasyTestRuntime", TestRuntime { suite, tests, report ->
            fabricApi.configureTests {
                createSourceSet.set(false)
                enableClientGameTests.set(false)
            }
            loom.mods {
                register(suite.modId) { sourceSet(sourceSets.getByName("main")) }
                register(suite.module.id) { sourceSet(tests) }
            }
            loom.runs.named("gameTest") {
                source(tests)
                runDir("build/run/test")
                property("fabric-api.gametest.report-file", report.get().asFile.absolutePath)
            }
        })
    }
}
