# Mod metadata and build targets

The mod owns its identity and target declarations. The shared plugin has no dependency
on the consuming project's `definitions` package.

```kotlin
import fantasy.gradle.*

plugins {
    id("fantasy.build.platform")
    id("fantasy.build.resources")
}

val metadata = modMetadata {
    id = "example_mod"
    name = "Example Mod"
    group = "example.mod"
    license = "All Rights Reserved"
    authors = "Example Author"
}

val targets = buildTargets(releaseVersion = "1.0") {
    fabric("1.21.1") {
        fabricApi()
        clothConfig()
    }
}

configureMod(metadata, targets.single())
```

`configureMod` sets the Java version, group, artifact version and archive name, exposes
`Target` and `ModMetadata` as project extensions, and registers `generateModMetadata`.
The example above needs a `loaders/fabric/src/main/templates/fabric.mod.json` containing at least
`{"schemaVersion": 1}`; entrypoints and mixins belong in this template.

`MinecraftVersions` owns the verified Minecraft/Java versions, loader and optional
library defaults, recipe/model formats, pack formats and test registration APIs.
Projects select targets; they do not maintain a second version catalog. A version
argument overrides a loader or library default, e.g. `forge("1.20.1", version = "47.3.0")`.
The low-level overload accepting `MinecraftRuntime` remains available for custom adapters.

Targets are explicit: `forge`, `neoforge` and `fabric` do not infer other combinations.
Use `includeModTargets(targets)` in a settings plugin to include their project paths.
The catalog's release version can be overridden inside a target. `minecraftRange`
can also be set explicitly; otherwise the existing minimum-version conventions apply.

## Dependencies

No target requires GeckoLib, Cloth Config or Fabric API by default. Declare `geckolib`,
`clothConfig` or `fabricApi` where needed. The latter two integrations support Fabric.
`requiredMod(id, version)` adds another required mod without changing the plugin.
Custom integrations can implement `ModDependency` and be added with `dependency(...)`.

Declarations supply both metadata requirements and dependency notations. The optional [loader adapters](loaders.md) apply Loom/ForgeGradle/ModDevGradle, declare
repositories, and attach supported libraries through their native dependency API. A custom
adapter can use the same notations, for example:

```kotlin
modImplementation(target.dependency<FabricApi>().notation)
modApi(target.dependency<ClothConfig>().notation)
```

`GeckoLib.notation(target)` selects the loader artifact; `modelDirectory` supplies the
GeckoLib 4/5 resource layout. These helpers are used only when GeckoLib is declared.

## Metadata templates

Fabric templates retain entrypoints, mixins and other custom fields. Managed fields
such as `id`, `version` and `depends` must come from the declarations, not the template.

Templates normally live in `loaders/<loader>/src/main/templates`.
A version-specific `loaders/<loader>/<minecraft>/src/main/templates` directory can
replace them when the entrypoints or metadata actually differ. A standalone
consumer can still use `src/main/templates` or pass a directory explicitly.
Forge and NeoForge expand these templates with Gradle.
The shared fields include `mod_id`, `mod_name`, `mod_version`, `mod_license`,
`mod_authors`, `mod_description`, `mod_icon`, and platform version ranges.
Place `${mod_dependencies}` after the platform dependencies to generate all declared
library dependencies. This uses `mandatory=true` for Forge and `type="required"` for
NeoForge 1.21.1. Templates retain the platform dependency tables and custom fields.

The formats follow the official [Forge dependency configuration](https://docs.minecraftforge.net/en/1.20.1/gettingstarted/modfiles/#dependency-configurations),
[NeoForge 1.21.1 metadata](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles/),
and [Fabric schema 1 specification](https://wiki.fabricmc.net/documentation:fabric_mod_json_spec).

## Sources, content and commands

After `configureMod`, call `configureModSources()` to attach the core's exported
sources/resources and the common, Minecraft, loader and loader/Minecraft source paths.
`configureModResources()` attaches `resources/`; when GeckoLib is declared it also
moves authored `geo/` models to the version's required output directory.
`generateModConstants(packageName, values)` exports generated Java string constants
from a core project using `fantasy.build.shared`.

Armor and weapon declarations live in `fantasy.gradle.content.armor` and
`fantasy.gradle.content.weapons`. They retain separate domain builders and share
recipe encodings, JSON serialization, translations and collision validation from
`fantasy.gradle.resources`. Content, IDs, attributes and localized text belong to the mod.
Namespaces, creative tab IDs and armor upgrade additions must be supplied explicitly.

Apply `fantasy.build.lifecycle` at the root for `generateResources`, `assemble`,
`test`, `check`, `build` and `clean`. `assemble` excludes tests; `test` runs unit and
server tests. `-PtestProfile=configured` selects the mod's configuration fixture.

`configureModTests(TestSuite(...))` uses the current target's native test runtime.
The mod supplies `TestModule`, test cases, source paths, expectation resources,
configuration fixtures. Reporter installation, registration APIs, metadata,
empty structures, native run configuration and strict XML verification belong to
the plugin. Test scenarios stay in the mod. Results end with a per-target PASS/FAIL
summary; XML reports are under `build/reports/test/<profile>/results.xml`.

A new Minecraft version must first be verified in `MinecraftVersions.kt`, then
selected by the mods. Add only the source adapters required by their actual code.

The pinned loader/library versions are tested defaults, not versions mandated by
Minecraft. Resource and test API profiles follow the versioned official references:
[Forge 1.20.1 registration and runs](https://docs.minecraftforge.net/en/1.20.1/misc/gametest/#registering-a-gametest),
[NeoForge 1.21.1 tests](https://docs.neoforged.net/docs/1.21.1/misc/gametest/),
[Fabric API 0.116.7 for 1.21.1](https://maven.fabricmc.net/docs/fabric-api-0.116.7+1.21.1/net/fabricmc/fabric/api/gametest/v1/FabricGameTest.html),
[Minecraft 1.21 pack formats](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21),
[1.21.9 pack metadata, retained by 1.21.10](https://www.minecraft.net/en-us/article/minecraft-java-edition-1-21-9),
and [1.21.11 formats](https://www.minecraft.net/nl-nl/article/minecraft-java-edition-1-21-11).
For registration changes beyond these older test APIs, the pinned loader/API
sources and each mod's server matrix are the verification boundary.

Fabric mod packs use Fabric API's native metadata fallback instead of shipping a
shared `pack.mcmeta`: it chooses the format separately for client resources and
server data. Forge's generated metadata follows its own
[1.21.11 MDK output](https://github.com/MinecraftForge/MinecraftForge/blob/1.21.11/src/test/generated/mdk_datagen/pack.mcmeta),
including data format 94.1. Test scenes are data-only packs.
