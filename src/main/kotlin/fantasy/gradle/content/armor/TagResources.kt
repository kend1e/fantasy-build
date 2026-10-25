package fantasy.gradle.content.armor

import fantasy.gradle.resources.json
import fantasy.gradle.minecraft.*

internal fun tagResources(namespace: String, armor: List<ArmorDefinition>, format: ArmorTagFormat): Map<String, String> = buildMap {
    if (format == ArmorTagFormat.NONE) return@buildMap

    val dyeTag = if (format == ArmorTagFormat.CAULDRON_AND_ENCHANTABLE) "cauldron_can_remove_dye" else "dyeable"
    put("data/minecraft/tags/item/$dyeTag.json", tag(armor.flatMap { set ->
        Piece.entries.map { "$namespace:${set.id}_${it.id}" }
    }))
    val enchantableTags = mapOf(
        Piece.HELMET to "head_armor",
        Piece.CHESTPLATE to "chest_armor",
        Piece.LEGGINGS to "leg_armor",
        Piece.BOOTS to "foot_armor",
    )
    for ((piece, name) in enchantableTags) {
        put("data/minecraft/tags/item/enchantable/$name.json", tag(armor.map {
            "$namespace:${it.id}_${piece.id}"
        }))
    }
}

private fun tag(values: List<String>): String = json(TagResource(values))
