package fantasy.gradle

import java.io.Serializable

enum class Loader(val id: String, val metadataFile: String) {
    FORGE("forge", "META-INF/mods.toml"),
    NEOFORGE("neoforge", "META-INF/neoforge.mods.toml"),
    FABRIC("fabric", "fabric.mod.json");

    fun minimumVersion(version: String): String = when (this) {
        FABRIC -> ">=$version"
        else -> "[$version,)"
    }

    fun languageVersion(version: String): String = when (this) {
        FORGE -> minimumVersion(version.substringBefore('.'))
        NEOFORGE -> minimumVersion("1")
        FABRIC -> minimumVersion(version)
    }
}

sealed interface LoaderSettings : Serializable {
    val loader: Loader
    val version: String
}

data class ForgeSettings(override val version: String) : LoaderSettings {
    override val loader get() = Loader.FORGE
}

data class NeoForgeSettings(override val version: String) : LoaderSettings {
    override val loader get() = Loader.NEOFORGE
}

data class FabricSettings(override val version: String) : LoaderSettings {
    override val loader get() = Loader.FABRIC
}
