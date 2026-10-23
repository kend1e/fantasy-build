# Loader adapters

The shared build has three optional adapter artifacts: `fabric`, `forge`, and `neoforge`.
Each depends on the core `fantasy-build` artifact and its native build plugins. Their
versions live in `gradle/libs.versions.toml` in this repository.

An included convention build can depend on the adapters it needs:

```kotlin
dependencies {
    implementation("net.kenddie.fantasy:fantasy-build:0.1.0-SNAPSHOT")
    implementation("net.kenddie.fantasy:fabric:0.1.0-SNAPSHOT")
}
```

Configure metadata and a target before applying its adapter:

```kotlin
configureMod(metadata, target)
pluginManager.apply("fantasy.build.fabric")
```

The IDs are `fantasy.build.fabric`, `fantasy.build.forge`, and `fantasy.build.neoforge`.
They configure official development mappings, loader dependencies, client/server runs,
declared GeckoLib/Fabric API/Cloth dependencies, and `exportRelease`. Known library
declarations select their repositories; undeclared libraries are not added. Arbitrary
`requiredMod` entries remain metadata requirements; attach their Maven dependencies
with the loader's native API.

The adapters select the installable artifact: Loom's `remapJar`, Renamer's `renameJar`
for SRG Forge, and `jar` for official-name Forge and NeoForge. Mod-specific sources,
mixins, processors, dependencies and test scenarios stay in the mod. `configureModTests` connects a declared test suite through each adapter’s native run model.

## Target options

```kotlin
forge("1.20.1")
fabric("1.20.1") {
    fabricApi()
    dataGeneration = true
}
```

`dataGeneration` defaults to false. Enabling it registers the native data run and
adds `build/generated/resources/datagen` to resources. Fabric requires Fabric API.
`names` is a Forge-only option; the adapter checks it against the verified runtime profile.

The current profiles cover the existing Armor targets: Fabric 1.20.1/1.21.1/1.21.10,
Forge 1.19.4/1.20.1/1.21.1/1.21.11 and NeoForge 1.21.1. Unknown versions fail with a
clear error. A new version needs a verified profile rather than a guessed compatibility rule.

Forge 1.19.4/1.20.1 use SRG output names and a separate remapping adapter. Forge
1.21.1 retains the Minecraft-compatible jopt-simple constraint. Forge 1.21.11 has
a separate EventBus 7 profile. Native APIs remain available for version-specific setup.

## Checks and limitations

`./gradlew check` validates all adapter plugins, runs the core tests, and configures
minimal consumers without GeckoLib/Cloth. The latter use the invoking Gradle user home
for cached toolchains and currently run offline; prime the Minecraft caches before running.
The mod matrices build release JARs and run the actual server scenarios.
Client rendering is not covered by these checks.

Configuration cache is disabled for building this repository: precompiled accessor
generation applies Loom/ForgeGradle during task execution. Core consumer tests still
check configuration-cache reuse explicitly. Loader builds do not claim support for it.

The adapters use native [Gradle convention plugins](https://docs.gradle.org/current/userguide/implementing_gradle_plugins_precompiled.html#sec:applying_external_plugins),
[ForgeGradle 7 APIs](https://github.com/MinecraftForge/ForgeGradle/blob/FG_7.0/docs/Overview.md),
Loom 1.13.6 and ModDevGradle 2.0.107. Exact plugin APIs were checked against their
resolved source artifacts and compiled as part of the adapter checks.
