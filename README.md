# Fantasy Build

Reusable Gradle conventions for Minecraft Fantasy Armor and Fantasy Weapons mods.

Build with JDK 25. Keep this repository beside the consuming mod at `../fantasy-build`.

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

For Minecraft platforms, declare identity with `modMetadata { ... }` and targets with
`buildTargets(releaseVersion = "1.0") { fabric("26.3") { fabricApi(); clothConfig() } }`.
`MinecraftVersions` supplies verified Java, loader, library, and resource-format defaults.
Declare optional libraries with `fabricApi()`, `clothConfig()`, or `geckolib()`; explicit
version arguments override the defaults.

In the mod's convention build, depend on `net.kenddie.fantasy:fantasy-build:0.1.0-SNAPSHOT`
and the needed `fabric`, `forge`, or `neoforge` adapter at the same version. Include this
checkout in that build's `pluginManagement` too. Call `configureMod(metadata, target)`
before applying `fantasy.build.fabric`, `fantasy.build.forge`, or `fantasy.build.neoforge`.
Metadata templates belong in `loaders/<loader>/src/main/templates/`; platform overrides
belong in `platforms/<target>/src/main/templates/`. Adapters configure native builds,
client/server runs, and release JAR export. Content and game scenarios stay in the mod.

Build plugin versions are in [gradle/libs.versions.toml](gradle/libs.versions.toml);
Minecraft defaults are in [MinecraftVersions.kt](src/main/kotlin/fantasy/gradle/minecraft/MinecraftVersions.kt).
Run `./gradlew check` for plugin validation and tests. Loader integration tests run
offline and need the corresponding Minecraft dependencies cached beforehand.
Client rendering is not covered.

_P.S. I also want to note that part of this code was written using AI, which isn't something I'm proud of, but I'm tired of Minecraft development and constantly having to update the API for every new version. I’m familiar with 1.7.10, but I don’t want to delve deeply into how the new versions of Minecraft work, let alone three or more loaders at the same time. I tried to keep it under control as best I could. Sorry if there’s any garbage code or bad design choices here._
