package fantasy.gradle

import java.io.Serializable

enum class ModEnvironment(val id: String) { ALL("*"), CLIENT("client"), SERVER("server") }

data class ModMetadata(
    val id: String,
    val name: String,
    val group: String,
    val license: String,
    val authors: String = "",
    val description: String = "",
    val homepage: String = "",
    val sources: String = "",
    val icon: String = "",
    val environment: ModEnvironment = ModEnvironment.ALL,
    val releaseName: String = id.replace('_', '-'),
) : Serializable {
    init {
        require(id.matches(Regex("[a-z][a-z0-9_-]{1,63}"))) { "Invalid mod ID: $id" }
        require(listOf(name, group, license, releaseName).all(String::isNotBlank)) { "$id: name, group, license and release name are required" }
        require(releaseName.matches(Regex("[A-Za-z0-9][A-Za-z0-9._-]*"))) { "$id: invalid release name: $releaseName" }
    }

    internal fun validate(target: Target) {
        if (target.loader != Loader.FABRIC) {
            require(id.matches(Regex("[a-z][a-z0-9_]{1,63}"))) { "${target.name}: invalid Forge/NeoForge mod ID: $id" }
        }
    }
}

@ModBuildDsl
class ModMetadataBuilder internal constructor() {
    var id: String? = null
    var name: String? = null
    var group: String? = null
    var license: String? = null
    var authors: String = ""
    var description: String = ""
    var homepage: String = ""
    var sources: String = ""
    var icon: String = ""
    var environment: ModEnvironment = ModEnvironment.ALL
    var releaseName: String? = null

    internal fun build(): ModMetadata {
        val id = requireNotNull(id) { "Mod ID is required" }
        return ModMetadata(
            id, requireNotNull(name) { "$id: mod name is required" },
            requireNotNull(group) { "$id: mod group is required" },
            requireNotNull(license) { "$id: mod license is required" },
            authors, description, homepage, sources, icon, environment, releaseName ?: id.replace('_', '-'),
        )
    }
}

fun modMetadata(configure: ModMetadataBuilder.() -> Unit): ModMetadata = ModMetadataBuilder().apply(configure).build()
