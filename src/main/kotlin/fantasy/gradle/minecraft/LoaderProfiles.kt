package fantasy.gradle.minecraft

import fantasy.gradle.ForgeNames
import fantasy.gradle.Loader
import fantasy.gradle.testing.TestRegistration

sealed interface LoaderProfile {
    val version: String
    val tests: TestRegistration
    val geckolib: String
    val serverConfigDirectory: String
}

data class ForgeProfile(
    override val version: String,
    override val tests: TestRegistration,
    override val geckolib: String,
    val names: ForgeNames = ForgeNames.OFFICIAL,
    val classicLaunch: Boolean = true,
    override val serverConfigDirectory: String = "world/serverconfig",
) : LoaderProfile

data class NeoForgeProfile(
    override val version: String,
    override val tests: TestRegistration,
    override val geckolib: String,
) : LoaderProfile {
    override val serverConfigDirectory = "config"
}

data class FabricProfile(
    override val version: String,
    override val tests: TestRegistration,
    override val geckolib: String,
    val fabricApi: String,
    val clothConfig: String,
) : LoaderProfile {
    override val serverConfigDirectory = "config"
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
