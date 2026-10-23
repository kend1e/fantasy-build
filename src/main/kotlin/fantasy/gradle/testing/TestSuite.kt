package fantasy.gradle.testing

import fantasy.gradle.GenerateFiles
import fantasy.gradle.Loader
import fantasy.gradle.PrepareTestRun
import fantasy.gradle.Target
import fantasy.gradle.minecraft.MinecraftVersions
import fantasy.gradle.resources.json
import fantasy.gradle.resources.packMetadata
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*

data class TestSuite(
    val module: TestModule,
    val cases: List<TestCase>,
    val sourceDirectories: List<String>,
    val profile: String,
    val resources: Map<String, String>,
    val configFiles: Map<String, String>,
    val configured: Boolean,
    val modId: String,
    val displayName: String,
    val license: String,
)

fun Project.configureModTests(suite: TestSuite): SourceSet {
    val module = suite.module
    val target = extensions.getByType<Target>()
    val version = MinecraftVersions[target.minecraft]
    val registration = version.loader(target.loader).tests
    val cases = suite.cases.filter { it.appliesTo(target) }
    val sourceSets = extensions.getByType<SourceSetContainer>()
    val main = sourceSets.getByName("main")
    val tests = sourceSets.getByName("test")
    val sourceRoot = rootProject.layout.projectDirectory
    suite.sourceDirectories.forEach { tests.java.srcDir(sourceRoot.dir(it)) }
    configurations.getByName(tests.implementationConfigurationName)
        .extendsFrom(configurations.getByName(main.implementationConfigurationName))
    configurations.getByName(tests.runtimeOnlyConfigurationName)
        .extendsFrom(configurations.getByName(main.runtimeOnlyConfigurationName))
    tests.compileClasspath += main.output + main.compileClasspath
    tests.runtimeClasspath += main.output + main.runtimeClasspath
    val generatedSources = tasks.register<GenerateFiles>("generateTestSources") {
        contents.set(buildMap {
            put("${module.packageName.replace('.', '/')}/${module.className}.java", registration.entrypoint(cases, module))
            if (!registration.fabric) {
                val loaderPackage = if (target.loader == Loader.NEOFORGE) "net.neoforged.fml.common.Mod" else "net.minecraftforge.fml.common.Mod"
                put("${module.packageName.replace('.', '/')}/TestMod.java", """
                    package ${module.packageName};
                    @$loaderPackage("${module.id}")
                    public final class TestMod {
                        public TestMod(${if (registration == TestRegistration.FORGE_FUNCTIONS) "net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext context" else if (registration == TestRegistration.NEOFORGE_FUNCTIONS) "net.neoforged.bus.api.IEventBus bus" else ""}) throws Exception {
                            String report = System.getProperty("${module.reportProperty}");
                            if (report == null) throw new IllegalStateException("Missing test report path");
                            net.minecraft.gametest.framework.GlobalTestReporter.replaceWith(
                                new net.minecraft.gametest.framework.JUnitLikeTestReporter(new java.io.File(report)));
                            ${if (registration == TestRegistration.FORGE_FUNCTIONS) "${module.className}.register(context.getModBusGroup());" else if (registration == TestRegistration.NEOFORGE_FUNCTIONS) "${module.className}.register(bus);" else ""}
                        }
                    }
                """.trimIndent())
            }
        })
        outputDirectory.set(layout.buildDirectory.dir("generated/sources/test"))
    }
    val metadata = tasks.register<GenerateFiles>("generateTestMetadata") {
        contents.set(buildMap {
            if (registration.fabric) {
                put("fabric.mod.json", json(mapOf(
                    "schemaVersion" to 1, "id" to "${module.id}", "version" to "1.0", "environment" to "*",
                    "entrypoints" to mapOf("fabric-gametest" to listOf("${module.packageName}.${module.className}")),
                    "depends" to mapOf(suite.modId to "*"),
                )))
            } else {
                val file = if (target.loader == Loader.NEOFORGE) "neoforge.mods.toml" else "mods.toml"
                put("META-INF/$file", """
                    modLoader="javafml"
                    loaderVersion="${target.loaderRange}"
                    license="${suite.license}"
                    [[mods]]
                    modId="${module.id}"
                    version="1.0"
                    displayName="${suite.displayName}"
                """.trimIndent())
            }
            putAll(suite.resources)
            if (registration.functions) {
                cases.forEach { case ->
                    put("data/${module.id}/test_instance/${case.id}.json", json(mapOf(
                        "type" to "minecraft:function", "function" to "${module.id}:${case.id}",
                        "environment" to "minecraft:default", "structure" to "${module.id}:empty", "max_ticks" to 100,
                    )))
                }
            }
            put("pack.mcmeta", version.packMetadata(suite.displayName, dataOnly = true))
        })
        outputDirectory.set(layout.buildDirectory.dir("generated/resources/test/metadata"))
    }
    val empty = emptyStructure()
    val structures = tasks.register<org.gradle.api.tasks.Sync>("prepareTestStructures") {
        from(empty) { into("data/${module.id}/${version.structures}") }
        into(layout.buildDirectory.dir("generated/resources/test/scenes"))
    }
    tests.java.srcDir(generatedSources.flatMap { it.outputDirectory })
    tests.resources.srcDir(metadata.flatMap { it.outputDirectory })
    tests.resources.srcDir(structures)
    tasks.named<Test>("test") {
        exclude("${module.packageName.replace('.', '/')}/**")
        failOnNoDiscoveredTests = false
        description = "Runs server tests for ${target.name}."
    }
    val report = verifyModTestReport(suite, when (target.loader) { Loader.FABRIC -> "runGameTest"; Loader.NEOFORGE -> "runGameTestServer"; Loader.FORGE -> "runTestGameTestServer" })
    extensions.getByType<TestRuntime>().configure(suite, tests, report)
    return tests
}

private fun Project.verifyModTestReport(suite: TestSuite, taskName: String): Provider<RegularFile> {
    val module = suite.module
    val target = extensions.getByType<Target>()
    val version = MinecraftVersions[target.minecraft]
    val registration = version.loader(target.loader).tests
    val expected = suite.cases.filter { it.appliesTo(target) }.map { registration.reportName(it.id, module) }.toSet() + registration.additionalReportNames
    val prepare = tasks.register<PrepareTestRun>("prepareTestRun") {
        val files = suite.configFiles
        filesToRemove.set(files.keys)
        contents.set(if (suite.configured) files else emptyMap())
        runDirectory.set(layout.buildDirectory.dir("run/test"))
    }
    val report = layout.buildDirectory.file("reports/test/${suite.profile}/results.xml")
    tasks.matching { it.name == taskName }.configureEach {
        val run = this
        val summary = rootProject.tasks.findByName("testSummary")?.let { rootProject.tasks.named(it.name) }
            ?: rootProject.tasks.register("testSummary") {
                description = "Prints a short summary of the Minecraft tests run in this build."
                doFirst { logger.lifecycle("\nMinecraft tests (${suite.profile}):") }
            }
        summary.configure {
            mustRunAfter(run)
            doLast {
                if (run.state.executed && !run.state.skipped) {
                    val file = report.get().asFile
                    val result = readTestReport(file, expected)
                    logger.lifecycle("${target.name}: ${result.summary(serverFailed = run.state.failure != null)}")
                }
            }
        }
        finalizedBy(summary)
        outputs.file(report)
        outputs.upToDateWhen { false }
        dependsOn(prepare, tasks.named("classes"), tasks.named("testClasses"))
        doFirst {
            val file = report.get().asFile
            file.parentFile.mkdirs()
            if (file.exists() && !file.delete()) throw GradleException("Cannot clear previous test report: $file")
        }
        doLast {
            val file = report.get().asFile
            val result = readTestReport(file, expected)
            if (!result.successful) throw GradleException("${result.summary()}\nSee $file")
        }
    }
    tasks.named("test") { dependsOn(taskName) }
    return report
}

private fun Project.emptyStructure(): Provider<RegularFile> {
    val task = tasks.register<CopyTestStructure>("generateEmptyTestStructure") {
        destination.set(layout.buildDirectory.file("generated/test-structure/empty.nbt"))
    }
    return task.flatMap { it.destination }
}

fun interface TestRuntime {
    fun configure(suite: TestSuite, sources: SourceSet, report: Provider<RegularFile>)
}
