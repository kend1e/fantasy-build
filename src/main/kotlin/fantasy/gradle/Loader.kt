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

    fun versionRange(minimum: String, maximumExclusive: String): String = when (this) {
        FABRIC -> ">=$minimum <$maximumExclusive-"
        else -> "[$minimum,$maximumExclusive)"
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
    val iconMetadataKey: String
}

data class ForgeSettings(override val version: String) : LoaderSettings {
    override val loader get() = Loader.FORGE
    override val iconMetadataKey get() = "logoFile"
}

data class NeoForgeSettings(override val version: String) : LoaderSettings {
    override val loader get() = Loader.NEOFORGE
    override val iconMetadataKey: String
        get() {
            val parts = version.substringBefore('-').split('.').map(String::toInt)
            val major = parts[0]
            val minor = parts[1]
            // NeoForge 26.2.0.50-beta introduced the new mod list metadata.
            val modern = major > 26 || major == 26 && (minor > 2 || minor == 2 &&
                (parts.getOrElse(2) { 0 } > 0 || parts.getOrElse(3) { 0 } >= 50))
            return if (modern) "iconFile" else "logoFile"
        }
}

data class FabricSettings(override val version: String) : LoaderSettings {
    override val loader get() = Loader.FABRIC
    override val iconMetadataKey get() = "icon"
}
