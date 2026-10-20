package fantasy.gradle;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipFile;

import static org.junit.Assert.*;

public class PluginFunctionalTest {
    @Rule public TemporaryFolder temporary = new TemporaryFolder();

    @Test
    public void metadataIsGeneratedForEachLoaderWithoutImplicitLibraries() throws IOException {
        for (String loader : new String[] {"forge", "neoforge", "fabric"}) {
            for (boolean sharedTemplates : new boolean[] {false, true}) {
                Path root = consumer();
                String templateDirectory = sharedTemplates ? "loader-templates" : "src/main/templates";
                String loaderVersion = switch (loader) {
                    case "forge" -> "52.1.3";
                    case "neoforge" -> "21.1.206";
                    default -> "0.18.2";
                };
                write(root, "build.gradle.kts", """
                    import fantasy.gradle.*
                    plugins {
                        id("fantasy.build.platform")
                        id("fantasy.build.resources")
                    }
                    val mod = modMetadata {
                        id = "fantasy_weapons"
                        name = "Fantasy Weapons"
                        group = "example.weapons"
                        license = "All Rights Reserved"
                    }
                    val target = buildTargets("0.4") {
                        %s(MinecraftRuntime("1.21.1", 21), "%s") {
                            requiredMod("custom_library", "2.0")
                        }
                    }.single()
                    %s
                    """.formatted(loader, loaderVersion, sharedTemplates
                        ? "configureMod(mod, target, layout.projectDirectory.dir(\"loader-templates\"))"
                        : "configureMod(mod, target)"));
                String metadataPath;
                if (loader.equals("fabric")) {
                    metadataPath = "fabric.mod.json";
                    write(root, templateDirectory + "/" + metadataPath, "{\"schemaVersion\":1}");
                } else {
                    metadataPath = "META-INF/" + (loader.equals("forge") ? "mods.toml" : "neoforge.mods.toml");
                    write(root, templateDirectory + "/" + metadataPath, """
                        modLoader="javafml"
                        loaderVersion="${loader_version_range}"
                        license="${mod_license}"
                        [[mods]]
                        modId="${mod_id}"
                        version="${mod_version}"
                        displayName="${mod_name}"
                        ${mod_dependencies}
                        """);
                }
                runner(root, "assemble", "--configuration-cache").build();
                Path jar = root.resolve("build/libs/fantasy_weapons-0.4-1.21.1.jar");
                try (ZipFile zip = new ZipFile(jar.toFile())) {
                    String text = new String(zip.getInputStream(zip.getEntry(metadataPath)).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    assertTrue(text.contains("fantasy_weapons"));
                    assertTrue(text.contains("0.4-1.21.1"));
                    assertFalse(text.contains("geckolib"));
                    assertFalse(text.contains("cloth-config"));
                    assertFalse(text.contains("fabric-api"));
                    assertTrue(text.contains("custom_library"));
                    assertFalse(text.contains("${"));
                }
                BuildResult repeated = runner(root, "assemble", "--configuration-cache").build();
                assertTrue(repeated.getOutput(), repeated.getOutput().contains("Reusing configuration cache"));
            }
        }
    }

    @Test
    public void independentModsShareSourcesAndGenerateResourcesWithConfigurationCache() throws IOException {
        for (String modId : new String[] {"fantasy_armor", "fantasy_weapons"}) {
            Path root = temporary.newFolder(modId).toPath();
            write(root, "settings.gradle.kts", "rootProject.name = \"consumer\"\ninclude(\"core\", \"platform\")\n");
            write(root, "core/build.gradle.kts", """
                plugins { id("fantasy.build.shared") }
                fantasyPlatform { javaVersion.set(21) }
                fantasyResources {
                    generate("generateDefaults") { contents.put("defaults.json", "{}") }
                }
                """);
            write(root, "core/src/main/java/example/SharedValue.java", "package example; public class SharedValue {}\n");
            write(root, "platform/build.gradle.kts", """
                plugins {
                    id("fantasy.build.resources")
                    id("fantasy.build.platform")
                }
                version = "1.0"
                fantasyPlatform {
                    javaVersion.set(21)
                    archiveName.set("%s")
                    sources.from(layout.projectDirectory.dir("extra/java"))
                }
                val variant = providers.gradleProperty("variant").orElse("first")
                fantasyResources {
                    sources.from(layout.projectDirectory.dir("assets"))
                    generate("generateContentResources") {
                        contents.set(variant.map { mapOf("assets/%s/$it.txt" to it) })
                    }
                }
                dependencies {
                    add("sharedSourcesDependencies", project(mapOf("path" to ":core", "configuration" to "sharedSources")))
                    add("sharedResourcesDependencies", project(mapOf("path" to ":core", "configuration" to "sharedResources")))
                }
                """.formatted(modId, modId));
            write(root, "platform/extra/java/example/Consumer.java", "package example; public class Consumer extends SharedValue {}\n");
            write(root, "platform/assets/static.txt", "retained");

            BuildResult first = runner(root, ":platform:assemble", "--configuration-cache").build();
            assertEquals(TaskOutcome.SUCCESS, first.task(":platform:generateContentResources").getOutcome());
            Path jar = root.resolve("platform/build/libs/" + modId + "-1.0.jar");
            try (ZipFile zip = new ZipFile(jar.toFile())) {
                assertNotNull(zip.getEntry("example/SharedValue.class"));
                assertNotNull(zip.getEntry("example/Consumer.class"));
                assertNotNull(zip.getEntry("defaults.json"));
                assertNotNull(zip.getEntry("static.txt"));
                assertNotNull(zip.getEntry("assets/" + modId + "/first.txt"));
                assertTrue(zip.stream().allMatch(entry -> entry.getTimeLocal().getYear() == 1980));
            }
            BuildResult second = runner(root, ":platform:assemble", "--configuration-cache").build();
            assertTrue(second.getOutput(), second.getOutput().contains("Reusing configuration cache"));
            assertEquals(TaskOutcome.UP_TO_DATE, second.task(":platform:generateContentResources").getOutcome());

            runner(root, ":platform:assemble", "-Pvariant=second", "--configuration-cache").build();
            Path generated = root.resolve("platform/build/generated/resources/generateContentResources/assets/" + modId);
            assertFalse(Files.exists(generated.resolve("first.txt")));
            assertEquals("second", Files.readString(generated.resolve("second.txt")));
            assertEquals("retained", Files.readString(root.resolve("platform/assets/static.txt")));
        }
    }

    @Test
    public void rejectsDuplicateResources() throws IOException {
        Path root = consumer();
        write(root, "build.gradle.kts", """
            plugins {
                id("fantasy.build.platform")
                id("fantasy.build.resources")
            }
            fantasyPlatform { javaVersion.set(21) }
            fantasyResources {
                generate("generateContent") { contents.put("duplicate.json", "{}") }
            }
            """);
        write(root, "src/main/resources/duplicate.json", "{}");
        BuildResult result = runner(root, "processResources").buildAndFail();
        assertEquals(TaskOutcome.FAILED, result.task(":processResources").getOutcome());
        assertTrue(result.getOutput().contains("duplicate.json"));
    }

    @Test
    public void rejectsEscapingPathsBeforeDeletingExistingOutput() throws IOException {
        Path root = consumer();
        write(root, "build.gradle.kts", """
            plugins { id("fantasy.build.resources") }
            fantasyResources {
                generate("generateContent") {
                    contents.put("../outside.txt", "invalid")
                }
            }
            """);
        Path sentinel = root.resolve("build/generated/resources/generateContent/keep.txt");
        write(root, "build/generated/resources/generateContent/keep.txt", "retained");
        BuildResult result = runner(root, "generateResources").buildAndFail();
        assertTrue(result.getOutput().contains("File path escapes"));
        assertEquals("retained", Files.readString(sentinel));
        assertFalse(Files.exists(root.resolve("build/generated/resources/outside.txt")));
    }

    @Test
    public void preparesOnlyOwnedConfigurationFiles() throws IOException {
        Path root = consumer();
        write(root, "build.gradle.kts", """
            import fantasy.gradle.PrepareTestRun
            plugins { id("fantasy.build.resources") }
            tasks.register<PrepareTestRun>("prepareRun") {
                runDirectory.set(layout.projectDirectory.dir("run"))
                filesToRemove.set(setOf("config/owned.json"))
                contents.set(mapOf("config/profile.json" to "{}"))
            }
            """);
        write(root, "run/config/owned.json", "old");
        write(root, "run/config/unrelated.json", "retained");
        runner(root, "prepareRun", "--configuration-cache").build();
        assertFalse(Files.exists(root.resolve("run/config/owned.json")));
        assertEquals("retained", Files.readString(root.resolve("run/config/unrelated.json")));
        assertEquals("{}", Files.readString(root.resolve("run/config/profile.json")));
    }

    @Test
    public void exportsAnArtifactToANewDirectory() throws IOException {
        Path root = consumer();
        write(root, "build.gradle.kts", """
            import fantasy.gradle.ExportRelease
            plugins { id("fantasy.build.resources") }
            tasks.register<ExportRelease>("exportRelease") {
                artifact.set(layout.projectDirectory.file("input.jar"))
                destination.set(layout.projectDirectory.file("dist/release.jar"))
            }
            """);
        write(root, "input.jar", "artifact bytes");
        runner(root, "exportRelease", "--configuration-cache").build();
        assertArrayEquals(Files.readAllBytes(root.resolve("input.jar")), Files.readAllBytes(root.resolve("dist/release.jar")));
    }

    private Path consumer() throws IOException {
        Path root = temporary.newFolder().toPath();
        write(root, "settings.gradle.kts", "rootProject.name = \"consumer\"\n");
        return root;
    }

    private GradleRunner runner(Path root, String... tasks) {
        var args = new java.util.ArrayList<>(Arrays.asList(tasks));
        args.addAll(Arrays.asList("--offline", "--stacktrace", "--max-workers=2"));
        return GradleRunner.create().withProjectDir(root.toFile()).withPluginClasspath().withArguments(args);
    }

    private static void write(Path root, String name, String contents) throws IOException {
        Path file = root.resolve(name);
        Files.createDirectories(file.getParent());
        Files.writeString(file, contents);
    }
}
