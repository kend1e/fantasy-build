package fantasy.gradle.resources

import fantasy.gradle.minecraft.IngredientEncoding
import fantasy.gradle.minecraft.RecipeFormat
import fantasy.gradle.minecraft.ResultEncoding
import fantasy.gradle.minecraft.UpgradeRecipeMethod

fun validateResourceId(id: String) {
    require(id.matches(Regex("[a-z0-9_.-]+:[a-z0-9_./-]+"))) { "Invalid resource ID: $id" }
}

fun validatePattern(pattern: List<String>, ingredients: Set<String>, count: Int) {
    require(pattern.size in 1..3 && pattern.first().length in 1..3 && pattern.all { it.length == pattern.first().length }) {
        "Crafting patterns must be rectangular and at most 3 x 3"
    }
    val symbols = pattern.joinToString("").filter { it != ' ' }.map(Char::toString).toSet()
    require(symbols.isNotEmpty() && symbols == ingredients) { "Mismatched crafting ingredients" }
    require(count in 1..64) { "Crafting result count must be between 1 and 64" }
}

data class ShapedRecipeDefinition(
    val id: String,
    val pattern: List<String>,
    val ingredients: Map<String, String>,
    val result: String,
    val count: Int = 1,
) {
    init {
        require(id.matches(Regex("[a-z0-9_]+"))) { "Invalid recipe ID: $id" }
        validatePattern(pattern, ingredients.keys, count)
        (ingredients.values + result).forEach(::validateResourceId)
    }
}

fun IngredientEncoding.encode(id: String): Any = when (this) {
    IngredientEncoding.ITEM_OBJECT -> mapOf("item" to id)
    IngredientEncoding.ITEM_WITH_METADATA -> mapOf("item" to id, "data" to 0)
    IngredientEncoding.ITEM_ID -> id
}

fun RecipeFormat.ingredient(id: String, anyDamage: Boolean = false): Any {
    if (anyDamage && ingredients == IngredientEncoding.ITEM_WITH_METADATA) {
        return mapOf("item" to (itemAliases[id]?.id ?: id), "data" to 32767)
    }
    val item = itemAliases[id] ?: return ingredients.encode(id)
    require(ingredients != IngredientEncoding.ITEM_ID) { "Item aliases require object ingredients" }
    return buildMap<String, Any> {
        put("item", item.id)
        if (ingredients == IngredientEncoding.ITEM_WITH_METADATA) put("data", item.data ?: 0)
        else item.data?.let { put("data", it) }
    }
}

fun ResultEncoding.encode(id: String, count: Int = 1): Any = when (this) {
    ResultEncoding.ITEM_ID -> {
        require(count == 1) { "A string result cannot specify a count" }
        id
    }
    else -> buildMap<String, Any> {
        put(if (this@encode == ResultEncoding.ITEM_FIELD) "item" else "id", id)
        if (count != 1) put("count", count)
    }
}

fun shapedRecipe(recipe: ShapedRecipeDefinition, format: RecipeFormat): String = json(mapOf(
    "pattern" to recipe.pattern,
    "key" to recipe.ingredients.mapValues { format.ingredient(it.value) },
    "result" to format.craftingResult.encode(recipe.result, recipe.count),
    "type" to "minecraft:crafting_shaped",
))

fun upgradeRecipe(template: String, base: String, addition: String, result: String, format: RecipeFormat): String =
    when (val method = format.upgrade) {
        UpgradeRecipeMethod.CraftingTable -> json(mapOf(
            "ingredients" to listOf(format.ingredient(template), format.ingredient(base, anyDamage = true), format.ingredient(addition)),
            "result" to format.craftingResult.encode(result),
            "type" to "minecraft:crafting_shapeless",
        ))
        is UpgradeRecipeMethod.SmithingTransform -> json(mapOf(
            "template" to format.ingredient(template),
            "base" to format.ingredient(base),
            "addition" to format.ingredient(addition),
            "result" to method.result.encode(result),
            "type" to "minecraft:smithing_transform",
            "category" to "equipment",
        ))
    }
