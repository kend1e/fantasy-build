package fantasy.gradle

import java.io.Serializable

interface ModDependency : Serializable {
    val id: String
    val version: String
    fun versionRange(loader: Loader): String = loader.minimumVersion(version)
    fun validate(loader: Loader) = Unit
}

data class RequiredMod(override val id: String, override val version: String) : ModDependency

data class GeckoLib(override val version: String) : ModDependency {
    override val id get() = "geckolib"

    val modelDirectory: String get() = when (version.substringBefore('.')) {
        "4" -> "geo"
        "5" -> "geckolib/models"
        else -> error("Unsupported GeckoLib model layout: $version")
    }

    fun notation(target: Target) = "software.bernie.geckolib:geckolib-${target.loader.id}-${target.minecraft}:$version"
}

data class FabricApi(override val version: String) : ModDependency {
    override val id get() = "fabric-api"
    val notation get() = "net.fabricmc.fabric-api:fabric-api:$version"
    override fun validate(loader: Loader) {
        require(loader == Loader.FABRIC) { "Fabric API can only be used with Fabric" }
    }
}

data class ClothConfig(override val version: String) : ModDependency {
    override val id get() = "cloth-config2"
    val notation get() = "me.shedaniel.cloth:cloth-config-fabric:$version"
    override fun validate(loader: Loader) {
        require(loader == Loader.FABRIC) { "The Cloth Config integration supports Fabric only" }
    }
}
