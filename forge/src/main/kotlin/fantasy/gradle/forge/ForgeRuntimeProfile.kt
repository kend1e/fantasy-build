package fantasy.gradle.forge

import fantasy.gradle.ForgeNames
import fantasy.gradle.Target

internal enum class ForgeRuntimeProfile(val names: ForgeNames, val classicLaunch: Boolean) {
    SRG(ForgeNames.SRG, true),
    OFFICIAL(ForgeNames.OFFICIAL, true),
    EVENTBUS_7(ForgeNames.OFFICIAL, false);

    companion object {
        private val versions = mapOf(
            "1.19.4" to SRG,
            "1.20.1" to SRG,
            "1.21.1" to OFFICIAL,
            "1.21.11" to EVENTBUS_7,
        )

        fun forTarget(target: Target): ForgeRuntimeProfile {
            val profile = versions[target.minecraft] ?: error("No verified Forge runtime profile for ${target.minecraft}")
            require(target.build.forgeNames == profile.names) { "${target.name}: expected ${profile.names} output names" }
            return profile
        }
    }
}
