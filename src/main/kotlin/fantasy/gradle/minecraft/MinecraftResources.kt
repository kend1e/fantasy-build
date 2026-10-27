package fantasy.gradle.minecraft

enum class IngredientEncoding { ITEM_OBJECT, ITEM_WITH_METADATA, ITEM_ID }
enum class TranslationFormat { JSON, LANG }

data class RecipeItem(val id: String, val data: Int? = null) {
    init {
        fantasy.gradle.resources.validateResourceId(id)
        require(data == null || data >= 0)
    }
}
enum class ResultEncoding { ITEM_FIELD, ID_FIELD, ITEM_ID }
enum class ItemModelFormat { PREDICATE_OVERRIDES, ITEM_DEFINITIONS }
enum class ArmorTagFormat { NONE, DYEABLE_AND_ENCHANTABLE, CAULDRON_AND_ENCHANTABLE }

sealed interface UpgradeRecipeMethod {
    object CraftingTable : UpgradeRecipeMethod
    data class SmithingTransform(val result: ResultEncoding) : UpgradeRecipeMethod
}

data class RecipeFormat(
    val directory: String,
    val ingredients: IngredientEncoding,
    val craftingResult: ResultEncoding,
    val upgrade: UpgradeRecipeMethod,
    val explicitDyeRecipes: Boolean = false,
    val root: String = "data",
    val itemAliases: Map<String, RecipeItem> = emptyMap(),
) {
    init {
        require(root in setOf("data", "assets"))
        require(directory == "recipes" || directory == "recipe") { "Unsupported recipe directory: $directory" }
        require(craftingResult != ResultEncoding.ITEM_ID) { "Crafting requires an object result" }
    }
}

data class MinecraftResources(
    val recipes: RecipeFormat,
    val itemModels: ItemModelFormat,
    val armorTags: ArmorTagFormat,
    val swordTags: Boolean,
    val translations: TranslationFormat = TranslationFormat.JSON,
)
