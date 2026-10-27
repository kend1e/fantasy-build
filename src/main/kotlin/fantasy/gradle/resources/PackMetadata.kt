package fantasy.gradle.resources

import fantasy.gradle.minecraft.MinecraftVersion

fun MinecraftVersion.packMetadata(description: String, dataOnly: Boolean = false): String {
    val format = if (dataOnly) requireNotNull(dataPackFormat) { "${runtime.id}: datapacks are not supported" }
        else dataPackFormat ?: resourcePackFormat
    return json(mapOf(
        "pack" to buildMap<String, Any> {
            put("description", description)
            put("pack_format", format.major)
            if (format.major >= 82) {
                put("min_format", if (format.minor == 0) format.major else listOf(format.major, format.minor))
                put("max_format", format.major)
            } else if (!dataOnly && dataPackFormat != null && resourcePackFormat != format) {
                put("supported_formats", mapOf(
                    "min_inclusive" to minOf(format.major, resourcePackFormat.major),
                    "max_inclusive" to maxOf(format.major, resourcePackFormat.major),
                ))
            }
        },
    ))
}
