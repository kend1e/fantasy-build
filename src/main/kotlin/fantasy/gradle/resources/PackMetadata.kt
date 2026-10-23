package fantasy.gradle.resources

import fantasy.gradle.minecraft.MinecraftVersion

fun MinecraftVersion.packMetadata(description: String, dataOnly: Boolean = false): String = json(mapOf(
    "pack" to buildMap<String, Any> {
        put("description", description)
        put("pack_format", dataPackFormat.major)
        if (dataPackFormat.major >= 82) {
            put("min_format", if (dataPackFormat.minor == 0) dataPackFormat.major else listOf(dataPackFormat.major, dataPackFormat.minor))
            put("max_format", dataPackFormat.major)
        } else if (!dataOnly && resourcePackFormat != dataPackFormat) {
            put("supported_formats", mapOf(
                "min_inclusive" to minOf(dataPackFormat.major, resourcePackFormat.major),
                "max_inclusive" to maxOf(dataPackFormat.major, resourcePackFormat.major),
            ))
        }
    },
))
