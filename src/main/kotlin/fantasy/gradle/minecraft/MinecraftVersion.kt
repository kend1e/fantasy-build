package fantasy.gradle.minecraft

import fantasy.gradle.Loader
import fantasy.gradle.MinecraftRuntime

data class PackFormat(val major: Int, val minor: Int = 0) {
    init {
        require(major > 0 && minor >= 0) { "Invalid pack format: $major.$minor" }
    }
}

data class MinecraftVersion(
    val runtime: MinecraftRuntime,
    val compatibilityUpperBound: String,
    val resources: MinecraftResources,
    val dataPackFormat: PackFormat?,
    val resourcePackFormat: PackFormat,
    val structures: String,
    val loaders: LoaderProfiles,
    val obfuscated: Boolean = true,
) {
    val forge: ForgeProfile get() = requireLoader(loaders.forge, Loader.FORGE)
    val neoforge: NeoForgeProfile get() = requireLoader(loaders.neoforge, Loader.NEOFORGE)
    val fabric: FabricProfile get() = requireLoader(loaders.fabric, Loader.FABRIC)

    fun loader(loader: Loader): LoaderProfile = requireLoader(loaders[loader], loader)

    private fun <T : LoaderProfile> requireLoader(profile: T?, loader: Loader): T =
        requireNotNull(profile) { "${runtime.id}: unsupported loader $loader" }
}
