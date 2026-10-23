package fantasy.gradle.content.armor

import fantasy.gradle.resources.json
import fantasy.gradle.minecraft.*

internal fun modelResources(namespace: String, materials: List<String>, armor: List<ArmorDefinition>, format: ItemModelFormat): Map<String, String> = buildMap {
    val assets = "assets/$namespace"
    val itemDefinitions = format == ItemModelFormat.ITEM_DEFINITIONS
    for (set in armor) {
        for (piece in Piece.entries) {
            val id = "${set.id}_${piece.id}"
            put("$assets/models/item/$id.json", json(baseItemModel(namespace, id, itemDefinitions)))
            put("$assets/models/item/${id}_dyed.json", json(dyedItemModel(namespace, id)))
            if (itemDefinitions) {
                put("$assets/items/$id.json", json(dyedItemDefinition(namespace, id)))
            }
        }
    }
    for (id in materials) {
        put("$assets/models/item/$id.json", json(ItemModelResource(textures = mapOf("layer0" to "$namespace:item/$id"))))
        if (itemDefinitions) put("$assets/items/$id.json", json(ItemDefinition(modelReference(namespace, id))))
    }
}

private fun baseItemModel(namespace: String, id: String, itemDefinitions: Boolean) = ItemModelResource(
    textures = mapOf("layer0" to "$namespace:item/$id"),
    overrides = if (itemDefinitions) null else listOf(ModelOverride(
        predicate = mapOf("$namespace:dyed" to 1.0),
        model = "$namespace:item/${id}_dyed",
    )),
)

private fun dyedItemModel(namespace: String, id: String) = ItemModelResource(
    textures = mapOf(
        "layer0" to "$namespace:item/$id",
        "layer1" to "$namespace:item/${id}_overlay",
    ),
)

private fun modelReference(namespace: String, id: String) = ModelReference("$namespace:item/$id")

private fun dyedItemDefinition(namespace: String, id: String) = ItemDefinition(
    DyedCondition(
        onTrue = ModelReference(
            model = "$namespace:item/${id}_dyed",
            tints = listOf(ConstantTint(-1), DyeTint()),
        ),
        onFalse = modelReference(namespace, id),
    ),
)
