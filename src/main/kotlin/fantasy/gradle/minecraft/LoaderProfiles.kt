package fantasy.gradle.minecraft

import fantasy.gradle.ForgeMetadata
import fantasy.gradle.ForgeNames
import fantasy.gradle.Loader
import fantasy.gradle.testing.TestRegistration

sealed interface LoaderProfile {
    val version: String
    val tests: TestRegistration?
    val geckolib: String?
    val testServerConfigDirectory: String
}

data class ForgeProfile(
    override val version: String,
    override val tests: TestRegistration? = null,
    override val geckolib: String? = null,
    val names: ForgeNames = ForgeNames.OFFICIAL,
    val classicLaunch: Boolean = true,
    val metadata: ForgeMetadata = ForgeMetadata.MODS_TOML,
    val mappingsChannel: String = "official",
    val mappingsVersion: String? = null,
    val launchJvmArgs: List<String> = emptyList(),
    override val testServerConfigDirectory: String = "world/serverconfig",
) : LoaderProfile

data class NeoForgeProfile(
    override val version: String,
    override val tests: TestRegistration,
    override val geckolib: String? = null,
) : LoaderProfile {
    override val testServerConfigDirectory = "config"
}

data class FabricProfile(
    override val version: String,
    override val tests: TestRegistration? = null,
    override val geckolib: String? = null,
    val fabricApi: String,
    val clothConfig: String,
    val fabricApiModId: String = "fabric-api",
    val clothConfigModule: String = "cloth-config-fabric",
    val autoConfig1u: String? = null,
) : LoaderProfile {
    override val testServerConfigDirectory = "config"
}

data class LoaderProfiles(
    val forge: ForgeProfile? = null,
    val neoforge: NeoForgeProfile? = null,
    val fabric: FabricProfile? = null,
) {
    operator fun get(loader: Loader): LoaderProfile? = when (loader) {
        Loader.FORGE -> forge
        Loader.NEOFORGE -> neoforge
        Loader.FABRIC -> fabric
    }
}
