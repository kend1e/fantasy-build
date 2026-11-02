package fantasy.gradle.content.armor

import fantasy.gradle.minecraft.MinecraftResources
import fantasy.gradle.minecraft.RecipeItem
import fantasy.gradle.resources.*

fun defaultResources(armor: List<ArmorDefinition>): Map<String, String> = mapOf(
    "armor_defaults.json" to json(armor.associate { it.id to it.pieces.mapKeys { piece -> piece.key.id } }),
    "effect_defaults.json" to json(armor.associate { it.id to it.effects }),
)

fun contentResources(namespace: String, armor: List<ArmorDefinition>, languages: TranslationCatalog,
                     craftingRecipes: List<ShapedRecipeDefinition>, formats: MinecraftResources, ingredientOverrides: Map<String, RecipeItem> = emptyMap()): Map<String, String> {
    require(languages.namespace == namespace) { "Translation namespace does not match content" }
    languages.validate(armor)
    val recipeFormat = formats.recipes.let { format ->
        format.copy(itemAliases = format.itemAliases + ingredientOverrides.mapValues { (_, item) -> format.itemAliases[item.id] ?: item })
    }
    val recipes = buildMap {
        val directory = "${recipeFormat.root}/$namespace/${recipeFormat.directory}"
        for (set in armor) for (piece in Piece.entries) {
            val id = "${set.id}_${piece.id}"
            if (recipeFormat.explicitDyeRecipes) {
                put("$directory/${id}_dyed.json", json(mapOf(
                    "type" to "minecraft:crafting_dye", "group" to "dyed_armor",
                    "dye" to "#minecraft:dyes", "target" to "$namespace:$id",
                    "result" to mapOf("id" to "$namespace:$id"),
                )))
            }
            put("$directory/$id.json", upgradeRecipe(set.upgradeIngredient, "minecraft:${formats.armorBaseMaterial}_${piece.id}", set.addition, "$namespace:$id", recipeFormat))
        }
        for (recipe in craftingRecipes) {
            val path = "$directory/${recipe.id}.json"
            require(putIfAbsent(path, shapedRecipe(recipe, recipeFormat)) == null) { "Duplicate recipe: $path" }
        }
    }
    return mergeResources(languages.resources(formats.translations), recipes,
        modelResources(namespace, craftingRecipes.map { it.id }, armor, formats.itemModels),
        tagResources(namespace, armor, formats.armorTags))
}
