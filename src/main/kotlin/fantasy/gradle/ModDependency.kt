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
    override val id get() = if (version.startsWith("3.")) "geckolib3" else "geckolib"

    val modelDirectory: String get() = when (version.substringBefore('.')) {
        "3", "4" -> "geo"
        "5" -> "geckolib/models"
        else -> error("Unsupported GeckoLib model layout: $version")
    }

    val group: String get() {
        val parts = version.split('.')
        val major = parts[0].toInt()
        val minor = parts[1].substringBefore('-').toInt()
        return if (major > 5 || major == 5 && minor >= 5) "com.geckolib" else "software.bernie.geckolib"
    }

    fun notation(target: Target): String {
        val minecraft = if (version.startsWith("3.")) when (target.minecraft) {
            "1.18.2" -> "1.18"
            "1.19.2" -> "1.19"
            else -> target.minecraft
        } else target.minecraft
        return "$group:geckolib-${target.loader.id}-$minecraft:$version"
    }
}

data class FabricApi(override val version: String, override val id: String = "fabric-api") : ModDependency {
    val notation get() = "net.fabricmc.fabric-api:fabric-api:$version"
    override fun validate(loader: Loader) {
        require(loader == Loader.FABRIC) { "Fabric API can only be used with Fabric" }
    }
}

data class ClothConfig(override val version: String, val module: String = "cloth-config-fabric") : ModDependency {
    override val id get() = "cloth-config2"
    val notation get() = "me.shedaniel.cloth:$module:$version"
    override fun validate(loader: Loader) {
        require(loader == Loader.FABRIC) { "The Cloth Config integration supports Fabric only" }
    }
}

data class AutoConfig1u(override val version: String) : ModDependency {
    override val id get() = "autoconfig1u"
    val notation get() = "me.sargunvohra.mcmods:autoconfig1u:$version"
    override fun validate(loader: Loader) {
        require(loader == Loader.FABRIC) { "AutoConfig1u supports Fabric only" }
    }
}
