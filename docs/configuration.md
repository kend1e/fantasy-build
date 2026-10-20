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
    fabric(MinecraftRuntime("1.21.1", java = 21), "0.18.2") {
        fabricApi("0.116.7+1.21.1")
        clothConfig("15.0.140")
    }
}

configureMod(metadata, targets.single())
```

`configureMod` sets the Java version, group, artifact version and archive name, exposes
`Target` and `ModMetadata` as project extensions, and registers `generateModMetadata`.
The example above needs a `src/main/templates/fabric.mod.json` containing at least
`{"schemaVersion": 1}`; entrypoints and mixins belong in this template.

Targets are explicit: `forge`, `neoforge` and `fabric` do not infer other combinations.
Use `includeModTargets(targets)` in a settings plugin to include their project paths.
The catalog's release version can be overridden inside a target. `minecraftRange`
can also be set explicitly; otherwise the existing minimum-version conventions apply.

## Dependencies

No target requires GeckoLib, Cloth Config or Fabric API by default. Declare `geckolib`,
`clothConfig` or `fabricApi` where needed. The latter two integrations support Fabric.
`requiredMod(id, version)` adds another required mod without changing the plugin.
Custom integrations can implement `ModDependency` and be added with `dependency(...)`.

Declarations supply both metadata requirements and dependency notations. The consuming
loader adapter still applies Loom/ForgeGradle/ModDevGradle, declares repositories, and
attaches the notations through its native dependency API, for example:

```kotlin
modImplementation(target.dependency<FabricApi>().notation)
modApi(target.dependency<ClothConfig>().notation)
```

`GeckoLib.notation(target)` selects the loader artifact; `modelDirectory` supplies the
GeckoLib 4/5 resource layout. These helpers are used only when GeckoLib is declared.

## Metadata templates

Fabric templates retain entrypoints, mixins and other custom fields. Managed fields
such as `id`, `version` and `depends` must come from the declarations, not the template.

Forge and NeoForge use Gradle template expansion for files under `src/main/templates`.
The shared fields include `mod_id`, `mod_name`, `mod_version`, `mod_license`,
`mod_authors`, `mod_description`, `mod_icon`, and platform version ranges.
Place `${mod_dependencies}` after the platform dependencies to generate all declared
library dependencies. This uses `mandatory=true` for Forge and `type="required"` for
NeoForge 1.21.1. Templates retain the platform dependency tables and custom fields.

The formats follow the official [Forge dependency configuration](https://docs.minecraftforge.net/en/1.20.1/gettingstarted/modfiles/#dependency-configurations),
[NeoForge 1.21.1 metadata](https://docs.neoforged.net/docs/1.21.1/gettingstarted/modfiles/),
and [Fabric schema 1 specification](https://wiki.fabricmc.net/documentation:fabric_mod_json_spec).
