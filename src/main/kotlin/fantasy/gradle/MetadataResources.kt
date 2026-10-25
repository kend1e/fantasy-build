package fantasy.gradle

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser

private val metadataGson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

fun ModMetadata.fabricValues(target: Target): Map<String, Any> {
    require(target.loader == Loader.FABRIC) { "Fabric metadata requires a Fabric target" }
    validate(target)
    return buildMap {
        put("id", id)
        put("version", target.modVersion)
        put("name", name)
        put("description", description.replace("\\n", "\n"))
        put("authors", authors.split(',').map(String::trim).filter(String::isNotEmpty))
        val contact = mapOf("homepage" to homepage, "sources" to sources).filterValues(String::isNotBlank)
        if (contact.isNotEmpty()) put("contact", contact)
        put("license", license)
        if (icon.isNotBlank()) put("icon", icon)
        put("environment", environment.id)
        put("depends", buildMap {
            put("fabricloader", target.loaderRange)
            put("minecraft", target.minecraftRange)
            put("java", ">=${target.java}")
            target.dependencies.forEach { put(it.id, it.versionRange(target.loader)) }
        })
    }
}

fun ModMetadata.templateValues(target: Target): Map<String, String> = buildMap {
    validate(target)
    put("minecraft_version", target.minecraft)
    put("minecraft_version_range", target.minecraftRange)
    put("loader_version_range", target.loaderRange)
    put("mod_id", id)
    put("mod_name", name)
    put("mod_license", license)
    put("mod_version", target.modVersion)
    put("mod_authors", authors)
    put("mod_description", description)
    put("mod_icon", icon)
    put("mod_icon_key", target.settings.iconMetadataKey)
    when (target.loader) {
        Loader.FORGE -> { put("forge_version", target.loaderVersion); put("forge_version_range", target.loaderDependencyRange) }
        Loader.NEOFORGE -> { put("neo_version", target.loaderVersion); put("neo_version_range", target.loaderDependencyRange) }
        Loader.FABRIC -> error("Fabric metadata uses fabricValues instead of TOML template values")
    }
    put("mod_dependencies", dependenciesToml(target))
}

private fun ModMetadata.dependenciesToml(target: Target): String {
    val required = when (target.loader) {
        Loader.FORGE -> "mandatory=true"
        Loader.NEOFORGE -> "type=\"required\""
        Loader.FABRIC -> error("Fabric dependencies use JSON")
    }
    return target.dependencies.joinToString("\n\n") { dependency ->
        """
            [[dependencies.$id]]
            modId=${metadataGson.toJson(dependency.id)}
            $required
            versionRange=${metadataGson.toJson(dependency.versionRange(target.loader))}
            ordering="NONE"
            side="BOTH"
        """.trimIndent()
    }
}

fun renderFabricMetadata(template: String, metadata: ModMetadata, target: Target): String {
    val document = JsonParser.parseString(template).asJsonObject
    metadata.fabricValues(target).forEach { (name, value) ->
        require(!document.has(name)) { "$name must be configured in mod metadata or the target, not fabric.mod.json" }
        document.add(name, metadataGson.toJsonTree(value))
    }
    return metadataGson.toJson(document) + "\n"
}
