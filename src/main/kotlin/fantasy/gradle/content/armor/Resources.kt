package fantasy.gradle.content.armor

import fantasy.gradle.minecraft.MinecraftResources
import fantasy.gradle.resources.*

fun defaultResources(armor: List<ArmorDefinition>): Map<String, String> = mapOf(
    "armor_defaults.json" to json(armor.associate { it.id to it.pieces.mapKeys { piece -> piece.key.id } }),
    "effect_defaults.json" to json(armor.associate { it.id to it.effects }),
)

fun contentResources(namespace: String, armor: List<ArmorDefinition>, languages: TranslationCatalog,
                     craftingRecipes: List<ShapedRecipeDefinition>, formats: MinecraftResources): Map<String, String> {
    require(languages.namespace == namespace) { "Translation namespace does not match content" }
    languages.validate(armor)
    val recipes = buildMap {
        val directory = "data/$namespace/${formats.recipes.directory}"
        for (set in armor) for (piece in Piece.entries) {
            val id = "${set.id}_${piece.id}"
            put("$directory/$id.json", upgradeRecipe(set.upgradeIngredient, "minecraft:netherite_${piece.id}", set.addition, "$namespace:$id", formats.recipes))
        }
        for (recipe in craftingRecipes) {
            val path = "$directory/${recipe.id}.json"
            require(putIfAbsent(path, shapedRecipe(recipe, formats.recipes)) == null) { "Duplicate recipe: $path" }
        }
    }
    return mergeResources(languages.resources(), recipes,
        modelResources(namespace, craftingRecipes.map { it.id }, armor, formats.itemModels),
        tagResources(namespace, armor, formats.armorTags))
}
