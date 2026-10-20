# Fantasy Build

Reusable Gradle conventions for Minecraft mods. Built and tested with Gradle 9.5.0 and JDK 21.

Connect the local checkout in `settings.gradle.kts`:

```kotlin
pluginManagement { includeBuild("../fantasy-build") }
```

Configure a platform project:

```kotlin
plugins {
    id("fantasy.build.platform")
    id("fantasy.build.resources")
}

fantasyPlatform {
    javaVersion.set(21)
    archiveName.set("my_mod")
    sources.from(rootProject.layout.projectDirectory.dir("common/java"))
}

fantasyResources {
    sources.from(rootProject.layout.projectDirectory.dir("resources"))
    generate("generateContentResources") {
        contents.put("assets/my_mod/lang/en_us.json", """{"item.my_mod.example":"Example"}""")
    }
}
```

`generateResources` prepares all declared resources. Java builds include them automatically.
Each generator owns its output directory; generated files never belong in source directories.

`fantasy.build.shared` exports `src/main/java` as `sharedSources` and processed resources as
`sharedResources`. Consumers add project dependencies to `sharedSourcesDependencies` and
`sharedResourcesDependencies`, selecting those outgoing configurations.

See [mod metadata and target configuration](docs/configuration.md) for the shared target DSL.

Run `./gradlew check` for plugin validation and functional tests. See [loader adapters](docs/loaders.md)
for platform setup and integration-test prerequisites. Content definitions and game scenarios stay
in the consuming mod. No plugin artifacts are published yet.
