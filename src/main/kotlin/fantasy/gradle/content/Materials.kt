package fantasy.gradle.content

import fantasy.gradle.resources.ShapedRecipeDefinition
import fantasy.gradle.resources.validatePattern
import fantasy.gradle.resources.validateResourceId

data class ShapedRecipe(val pattern: List<String>, val ingredients: Map<Char, String>, val count: Int = 1) {
    init {
        validatePattern(pattern, ingredients.keys.map(Char::toString).toSet(), count)
        ingredients.values.forEach(::validateResourceId)
    }
}

data class MaterialDefinition(val id: String, val recipe: ShapedRecipe) {
    init {
        require(id.matches(Regex("[a-z0-9_]+"))) { "Invalid material ID: $id" }
    }

    fun craftingRecipe(namespace: String) = ShapedRecipeDefinition(
        id, recipe.pattern, recipe.ingredients.mapKeys { it.key.toString() }, "$namespace:$id", recipe.count,
    )
}
