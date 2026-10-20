package fantasy.gradle.minecraft

import fantasy.gradle.MinecraftRuntime
import fantasy.gradle.Loader
import fantasy.gradle.ForgeNames
import fantasy.gradle.testing.TestRegistration

enum class IngredientEncoding { ITEM_OBJECT, ITEM_ID }
enum class ResultEncoding { ITEM_FIELD, ID_FIELD, ITEM_ID }
enum class ItemModelFormat { PREDICATE_OVERRIDES, ITEM_DEFINITIONS }
enum class ArmorTagFormat { NONE, DYEABLE_AND_ENCHANTABLE }

sealed interface UpgradeRecipeMethod {
    object CraftingTable : UpgradeRecipeMethod
    data class SmithingTransform(val result: ResultEncoding) : UpgradeRecipeMethod
}

data class RecipeFormat(
    val directory: String,
    val ingredients: IngredientEncoding,
    val craftingResult: ResultEncoding,
    val upgrade: UpgradeRecipeMethod,
) {
    init {
        require(directory == "recipes" || directory == "recipe") { "Unsupported recipe directory: $directory" }
        require(craftingResult != ResultEncoding.ITEM_ID) { "Crafting requires an object result" }
    }
}

data class MinecraftResources(
    val recipes: RecipeFormat,
    val itemModels: ItemModelFormat,
    val armorTags: ArmorTagFormat,
    val swordTags: Boolean,
)

sealed interface LoaderProfile {
    val version: String
    val tests: TestRegistration
    val geckolib: String
    val serverConfigDirectory: String
}

data class ForgeProfile(
    override val version: String,
    override val tests: TestRegistration,
    override val geckolib: String,
    val names: ForgeNames = ForgeNames.OFFICIAL,
    val classicLaunch: Boolean = true,
    override val serverConfigDirectory: String = "world/serverconfig",
) : LoaderProfile

data class NeoForgeProfile(
    override val version: String,
    override val tests: TestRegistration,
    override val geckolib: String,
) : LoaderProfile {
    override val serverConfigDirectory = "config"
}

data class FabricProfile(
    override val version: String,
    override val tests: TestRegistration,
    override val geckolib: String,
    val fabricApi: String,
    val clothConfig: String,
) : LoaderProfile {
    override val serverConfigDirectory = "config"
}

data class MinecraftVersion(
    val runtime: MinecraftRuntime,
    val resources: MinecraftResources,
    val dataPackFormat: Int,
    val resourcePackFormat: Int,
    val structures: String,
    val loaders: Map<Loader, LoaderProfile>,
    val dataPackMinor: Int = 0,
) {
    fun packMetadata(description: String, dataOnly: Boolean = false): String = fantasy.gradle.resources.json(mapOf("pack" to buildMap<String, Any> {
        put("description", description)
        put("pack_format", dataPackFormat)
        if (dataPackFormat >= 82) {
            put("min_format", if (dataPackMinor == 0) dataPackFormat else listOf(dataPackFormat, dataPackMinor))
            put("max_format", dataPackFormat)
        } else if (!dataOnly && resourcePackFormat != dataPackFormat) {
            put("supported_formats", mapOf("min_inclusive" to minOf(dataPackFormat, resourcePackFormat),
                "max_inclusive" to maxOf(dataPackFormat, resourcePackFormat)))
        }
    }))

    val forge get() = loader(Loader.FORGE) as ForgeProfile
    val fabric get() = loader(Loader.FABRIC) as FabricProfile

    fun loader(loader: Loader): LoaderProfile = requireNotNull(loaders[loader]) { "${runtime.id}: unsupported loader $loader" }
}

object MinecraftVersions {
    private fun format(directory: String, result: ResultEncoding, ingredients: IngredientEncoding = IngredientEncoding.ITEM_OBJECT,
                       upgrade: UpgradeRecipeMethod = UpgradeRecipeMethod.SmithingTransform(result)) = MinecraftResources(
        RecipeFormat(directory, ingredients, result, upgrade),
        if (ingredients == IngredientEncoding.ITEM_ID) ItemModelFormat.ITEM_DEFINITIONS else ItemModelFormat.PREDICATE_OVERRIDES,
        if (directory == "recipes") ArmorTagFormat.NONE else ArmorTagFormat.DYEABLE_AND_ENCHANTABLE,
        swordTags = directory == "recipe",
    )
    private val versions = listOf(
        MinecraftVersion(MinecraftRuntime("1.19.4", 17), format("recipes", ResultEncoding.ITEM_FIELD, upgrade = UpgradeRecipeMethod.CraftingTable), 12, 13, "structures", mapOf(
            Loader.FORGE to ForgeProfile("45.3.9", TestRegistration.FORGE_CONTAINER, "4.2", names = ForgeNames.SRG),
        )),
        MinecraftVersion(MinecraftRuntime("1.20.1", 17), format("recipes", ResultEncoding.ITEM_FIELD), 15, 15, "structures", mapOf(
            Loader.FORGE to ForgeProfile("47.3.12", TestRegistration.FORGE_CONTAINER, "4.4.7", names = ForgeNames.SRG),
            Loader.FABRIC to FabricProfile("0.16.7", TestRegistration.FABRIC_VANILLA, "4.4.7", "0.92.2+1.20.1", "11.1.136"),
        )),
        MinecraftVersion(MinecraftRuntime("1.21.1", 21), format("recipe", ResultEncoding.ID_FIELD), 48, 34, "structure", mapOf(
            Loader.FORGE to ForgeProfile("52.1.3", TestRegistration.FORGE_COMPONENTS, "4.7.7"),
            Loader.NEOFORGE to NeoForgeProfile("21.1.206", TestRegistration.NEOFORGE, "4.7.7"),
            Loader.FABRIC to FabricProfile("0.18.2", TestRegistration.FABRIC_VANILLA, "4.8.2", "0.116.7+1.21.1", "15.0.140"),
        )),
        MinecraftVersion(MinecraftRuntime("1.21.10", 21), format("recipe", ResultEncoding.ID_FIELD, IngredientEncoding.ITEM_ID,
            UpgradeRecipeMethod.SmithingTransform(ResultEncoding.ITEM_ID)), 88, 69, "structure", mapOf(
            Loader.FABRIC to FabricProfile("0.18.4", TestRegistration.FABRIC_REGISTRY, "5.3-alpha-3", "0.138.4+1.21.10", "20.0.149"),
        )),
        MinecraftVersion(MinecraftRuntime("1.21.11", 21), format("recipe", ResultEncoding.ID_FIELD, IngredientEncoding.ITEM_ID,
            UpgradeRecipeMethod.SmithingTransform(ResultEncoding.ITEM_ID)), 94, 75, "structure", mapOf(
            Loader.FORGE to ForgeProfile("61.2.0", TestRegistration.FORGE_FUNCTIONS, "5.4.5", classicLaunch = false,
                serverConfigDirectory = "gametestserver/gametestworld/serverconfig"),
        ), dataPackMinor = 1),
    ).associateBy { it.runtime.id }
    operator fun get(id: String): MinecraftVersion = requireNotNull(versions[id]) { "Unverified Minecraft version: $id" }
    val supported: Set<String> get() = versions.keys
}

