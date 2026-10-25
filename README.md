# Fantasy Build

Reusable Gradle conventions for Minecraft Fantasy Armor and Fantasy Weapons mods.

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

_P.S. I also want to note that part of this code was written using AI, which isn't something I'm proud of, but I'm tired of Minecraft development and constantly having to update the API for every new version. I’m familiar with 1.7.10, but I don’t want to delve deeply into how the new versions of Minecraft work, let alone three or more loaders at the same time. I tried to keep it under control as best I could. Sorry if there’s any garbage code or bad design choices here._
