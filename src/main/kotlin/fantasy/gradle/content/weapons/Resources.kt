package fantasy.gradle.content.weapons

import fantasy.gradle.minecraft.*
import fantasy.gradle.resources.*

fun catalogResources(content: ContentCatalog): Map<String, String> = mapOf(
    "weapon_catalog.json" to json(mapOf(
        "tabIcon" to content.tabIcon,
        "materials" to content.materials.map { it.id },
        "weapons" to content.weapons.map { mapOf("id" to it.id, "largeModel" to (it.largeModel != null)) },
    )),
)

fun contentResources(content: ContentCatalog, languages: TranslationCatalog, minecraft: String): Map<String, String> {
    val version = MinecraftVersions[minecraft]
    val format = version.resources.recipes
    require(languages.namespace == content.namespace)
    languages.validateItems(content.itemIds.toSet(), content.tabTranslationKey)
    return buildMap {
        val assets = "assets/${content.namespace}"
        val recipes = "data/${content.namespace}/${format.directory}"
        fun model(id: String, parent: String) = json(mapOf(
            "parent" to parent, "textures" to mapOf("layer0" to "${content.namespace}:item/$id"),
        ))
        for (material in content.materials) {
            put("$assets/models/item/${material.id}.json", model(material.id, "item/generated"))
            put("$recipes/${material.id}.json", shapedRecipe(material.craftingRecipe(content.namespace), format))
        }
        for (weapon in content.weapons) {
            put("$assets/models/item/${weapon.id}.json", model(weapon.id, "item/handheld"))
            weapon.largeModel?.let { large ->
                put("$assets/models/item/${weapon.id}_large.json", model("${weapon.id}_large", "${content.namespace}:item/${large.parent}"))
            }
            put("$recipes/${weapon.id}.json", upgradeRecipe(weapon.template, weapon.base, content.addition, "${content.namespace}:${weapon.id}", format))
        }
        if (version.resources.itemModels == ItemModelFormat.ITEM_DEFINITIONS) {
            fun reference(id: String) = mapOf("type" to "minecraft:model", "model" to "${content.namespace}:item/$id")
            for (material in content.materials) {
                put("$assets/items/${material.id}.json", json(mapOf("model" to reference(material.id))))
            }
            for (weapon in content.weapons) {
                val model = if (weapon.largeModel == null) reference(weapon.id) else mapOf(
                    "type" to "minecraft:select",
                    "property" to "minecraft:display_context",
                    "cases" to listOf(mapOf(
                        "when" to listOf("firstperson_lefthand", "firstperson_righthand", "thirdperson_lefthand", "thirdperson_righthand"),
                        "model" to reference("${weapon.id}_large"),
                    )),
                    "fallback" to reference(weapon.id),
                )
                put("$assets/items/${weapon.id}.json", json(mapOf("model" to model)))
            }
        }
        if (version.resources.swordTags) put("data/minecraft/tags/item/swords.json", json(mapOf(
            "replace" to false, "values" to content.weapons.map { "${content.namespace}:${it.id}" },
        )))
        putAll(languages.resources())
    }
}
