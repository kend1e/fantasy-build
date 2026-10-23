package fantasy.gradle.forge

import fantasy.gradle.ForgeNames
import fantasy.gradle.Target
import fantasy.gradle.Loader
import fantasy.gradle.minecraft.MinecraftVersions

internal enum class ForgeRuntimeProfile(val names: ForgeNames, val classicLaunch: Boolean) {
    SRG(ForgeNames.SRG, true),
    OFFICIAL(ForgeNames.OFFICIAL, true),
    EVENTBUS_7(ForgeNames.OFFICIAL, false);

    companion object {
        fun forTarget(target: Target): ForgeRuntimeProfile {
            val verified = MinecraftVersions[target.minecraft].forge
            val profile = entries.single { it.names == verified.names && it.classicLaunch == verified.classicLaunch }
            require(target.build.forgeNames == profile.names) { "${target.name}: expected ${profile.names} output names" }
            return profile
        }
    }
}
