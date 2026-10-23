package fantasy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

public class LoaderAdapterTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test public void fabricWithoutOptionalLibraries() throws Exception {
        probe("fabric", "1.21.1", "0.18.2", "", "remapJar", "", "runClient", "runServer");
    }

    @Test public void fabricWithoutRemapping() throws Exception {
        probe("fabric", "26.1.2", "0.20.1", "", "jar", "", "runClient", "runServer");
    }

    @Test public void forgeWithOfficialNames() throws Exception {
        probe("forge", "1.21.1", "52.1.3", "dataGeneration = true", "jar", "", "runClient", "runServer", "runData");
    }

    @Test public void forgeWithSrgNames() throws Exception {
        probe("forge", "1.20.1", "47.3.12", "names = ForgeNames.SRG", "renameJar", "srg", "runClient", "runServer", "devRefmapMappings");
    }

    @Test public void forgeWithEventbus7() throws Exception {
        probe("forge", "1.21.11", "61.2.0", "dataGeneration = true", "jar", "", "runClient", "runServer", "runData");
    }

    @Test public void neoforgeWithoutOptionalLibraries() throws Exception {
        probe("neoforge", "1.21.1", "21.1.206", "dataGeneration = true", "jar", "", "runClient", "runServer", "runData");
    }

    private void probe(String loader, String minecraft, String version, String options,
                       String artifactTask, String classifier, String... runTasks) throws Exception {
        Path root = temporary.newFolder().toPath();
        Files.writeString(root.resolve("settings.gradle.kts"), "rootProject.name = \"consumer\"\n");
        StringBuilder taskChecks = new StringBuilder();
        for (String task : runTasks) taskChecks.append("check(tasks.findByName(\"").append(task).append("\") != null)\n");
        Files.writeString(root.resolve("build.gradle.kts"), """
            import fantasy.gradle.*
            import fantasy.gradle.Target
            plugins {
                id("fantasy.build.platform")
                id("fantasy.build.resources")
            }
            val target = buildTargets("0.4") {
                %s("%s", "%s") { %s }
            }.single()
            configureMod(modMetadata {
                id = "fantasy_weapons"
                name = "Fantasy Weapons"
                group = "example.weapons"
                license = "All Rights Reserved"
            }, target)
            apply(plugin = "fantasy.build.%s")
            tasks.register("verifyAdapter") { doLast {
            %s
            val expectedArtifact = tasks.named("%s")
            val export = tasks.named<ExportRelease>("exportRelease")
            check(export.get().artifact.get().asFile == %s)
            check(export.get().destination.get().asFile.name == "fantasy-weapons-0.4-%s-%s.jar")
            val dependencyGroups = configurations.flatMap { it.dependencies }.filterIsInstance<ExternalModuleDependency>().map { it.group }
            check(dependencyGroups.none { it == "software.bernie.geckolib" || it == "me.shedaniel.cloth" })
            val expectedClassifier = "%s"
            if (expectedClassifier.isNotEmpty()) check(export.get().artifact.get().asFile.name.endsWith("-$expectedClassifier.jar"))
            println("ADAPTER_OK")
            } }
            """.formatted(loader, minecraft, version, options,
                loader, taskChecks, artifactTask,
                artifactTask.equals("renameJar")
                    ? "(expectedArtifact.get() as net.minecraftforge.renamer.gradle.RenameJar).output.get().asFile"
                    : "(expectedArtifact.get() as org.gradle.api.tasks.bundling.AbstractArchiveTask).archiveFile.get().asFile",
                minecraft, loader, classifier));
        Path templates = root.resolve("src/main/templates");
        Files.createDirectories(templates.resolve("META-INF"));
        if (loader.equals("fabric")) Files.writeString(templates.resolve("fabric.mod.json"), "{\"schemaVersion\":1}");
        else Files.writeString(templates.resolve("META-INF/" + (loader.equals("forge") ? "mods.toml" : "neoforge.mods.toml")), "modId=\"${mod_id}\"\n");
        var classpath = Arrays.stream(System.getProperty("adapterTestClasspath").split(File.pathSeparator)).map(File::new).toList();
        BuildResult result = GradleRunner.create().withProjectDir(root.toFile()).withPluginClasspath(classpath)
            .withArguments("verifyAdapter", "--gradle-user-home", System.getProperty("adapterGradleUserHome"), "--offline", "--no-configuration-cache", "--max-workers=2", "--stacktrace").build();
        assertTrue(result.getOutput(), result.getOutput().contains("ADAPTER_OK"));
    }
}
