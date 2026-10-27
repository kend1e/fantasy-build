package fantasy.gradle.minecraft

import fantasy.gradle.ForgeMetadata
import fantasy.gradle.ForgeNames
import fantasy.gradle.MinecraftRuntime
import fantasy.gradle.testing.TestRegistration

object MinecraftVersions {
    private val itemDefinitionResources = MinecraftResources(
        recipes = RecipeFormat(
            directory = "recipe",
            ingredients = IngredientEncoding.ITEM_ID,
            craftingResult = ResultEncoding.ID_FIELD,
            upgrade = UpgradeRecipeMethod.SmithingTransform(ResultEncoding.ITEM_ID),
        ),
        itemModels = ItemModelFormat.ITEM_DEFINITIONS,
        armorTags = ArmorTagFormat.DYEABLE_AND_ENCHANTABLE,
        swordTags = true,
    )

    private val versions = listOf(
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.12.2", java = 8),
            compatibilityUpperBound = "1.13",
            resources = MinecraftResources(
                recipes = RecipeFormat(
                    directory = "recipes", root = "assets",
                    ingredients = IngredientEncoding.ITEM_WITH_METADATA,
                    craftingResult = ResultEncoding.ITEM_FIELD,
                    upgrade = UpgradeRecipeMethod.CraftingTable,
                    itemAliases = mapOf(
                        "minecraft:slime_block" to RecipeItem("minecraft:slime"),
                        "minecraft:white_tulip" to RecipeItem("minecraft:red_flower", 6),
                        "minecraft:yellow_dye" to RecipeItem("minecraft:dye", 11),
                        "minecraft:magenta_dye" to RecipeItem("minecraft:dye", 13),
                        "minecraft:acacia_log" to RecipeItem("minecraft:log2", 0),
                        "minecraft:oak_log" to RecipeItem("minecraft:log", 0),
                        "minecraft:granite" to RecipeItem("minecraft:stone", 1),
                        "minecraft:diorite" to RecipeItem("minecraft:stone", 3),
                        "minecraft:andesite" to RecipeItem("minecraft:stone", 5),
                        "minecraft:pufferfish" to RecipeItem("minecraft:fish", 3),
                        "minecraft:magma_block" to RecipeItem("minecraft:magma"),
                        "minecraft:stone_bricks" to RecipeItem("minecraft:stonebrick"),
                        "minecraft:nether_bricks" to RecipeItem("minecraft:nether_brick"),
                        "minecraft:red_dye" to RecipeItem("minecraft:dye", 1),
                        "minecraft:red_sand" to RecipeItem("minecraft:sand", 1),
                        "minecraft:sugar_cane" to RecipeItem("minecraft:reeds"),
                        "minecraft:charcoal" to RecipeItem("minecraft:coal", 1),
                        "minecraft:sunflower" to RecipeItem("minecraft:double_plant", 0),
                    ),
                ),
                itemModels = ItemModelFormat.PREDICATE_OVERRIDES,
                armorTags = ArmorTagFormat.NONE,
                swordTags = false,
                translations = TranslationFormat.LANG,
            ),
            dataPackFormat = null,
            resourcePackFormat = PackFormat(3),
            structures = "structures",
            loaders = LoaderProfiles(forge = ForgeProfile(
                version = "14.23.5.2864", names = ForgeNames.SRG,
                metadata = ForgeMetadata.MCMOD_INFO,
                mappingsChannel = "stable", mappingsVersion = "39-1.12",
            )),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.14.4", java = 8),
            compatibilityUpperBound = "1.15",
            resources = MinecraftResources(
                recipes = RecipeFormat(
                    directory = "recipes", ingredients = IngredientEncoding.ITEM_OBJECT,
                    craftingResult = ResultEncoding.ITEM_FIELD, upgrade = UpgradeRecipeMethod.CraftingTable,
                ),
                itemModels = ItemModelFormat.PREDICATE_OVERRIDES,
                armorTags = ArmorTagFormat.NONE, swordTags = false,
            ),
            dataPackFormat = PackFormat(4), resourcePackFormat = PackFormat(4),
            structures = "structures",
            loaders = LoaderProfiles(
                forge = ForgeProfile(version = "28.2.30", names = ForgeNames.SRG),
                fabric = FabricProfile(version = "0.16.7",
                    fabricApi = "0.28.5+1.14", clothConfig = "1.8", fabricApiModId = "fabric", clothConfigModule = "config-2", autoConfig1u = "1.2.6"),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.15.2", java = 8),
            compatibilityUpperBound = "1.16",
            resources = MinecraftResources(
                recipes = RecipeFormat(
                    directory = "recipes", ingredients = IngredientEncoding.ITEM_OBJECT,
                    craftingResult = ResultEncoding.ITEM_FIELD, upgrade = UpgradeRecipeMethod.CraftingTable,
                ),
                itemModels = ItemModelFormat.PREDICATE_OVERRIDES,
                armorTags = ArmorTagFormat.NONE, swordTags = false,
            ),
            dataPackFormat = PackFormat(5), resourcePackFormat = PackFormat(5),
            structures = "structures",
            loaders = LoaderProfiles(
                forge = ForgeProfile(version = "31.2.62", names = ForgeNames.SRG),
                fabric = FabricProfile(version = "0.16.7",
                    fabricApi = "0.28.5+1.15", clothConfig = "2.14.2", fabricApiModId = "fabric", clothConfigModule = "config-2", autoConfig1u = "2.2.0"),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.16.5", java = 8),
            compatibilityUpperBound = "1.17",
            resources = MinecraftResources(
                recipes = RecipeFormat(
                    directory = "recipes", ingredients = IngredientEncoding.ITEM_OBJECT,
                    craftingResult = ResultEncoding.ITEM_FIELD, upgrade = UpgradeRecipeMethod.CraftingTable,
                ),
                itemModels = ItemModelFormat.PREDICATE_OVERRIDES,
                armorTags = ArmorTagFormat.NONE, swordTags = false,
            ),
            dataPackFormat = PackFormat(6), resourcePackFormat = PackFormat(6),
            structures = "structures",
            loaders = LoaderProfiles(
                forge = ForgeProfile(version = "36.2.42", names = ForgeNames.SRG),
                fabric = FabricProfile(version = "0.16.7",
                    fabricApi = "0.42.0+1.16", clothConfig = "4.17.101", fabricApiModId = "fabric"),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.18.2", java = 17),
            compatibilityUpperBound = "1.19",
            resources = MinecraftResources(
                recipes = RecipeFormat(
                    directory = "recipes", ingredients = IngredientEncoding.ITEM_OBJECT,
                    craftingResult = ResultEncoding.ITEM_FIELD, upgrade = UpgradeRecipeMethod.CraftingTable,
                ),
                itemModels = ItemModelFormat.PREDICATE_OVERRIDES,
                armorTags = ArmorTagFormat.NONE, swordTags = false,
            ),
            dataPackFormat = PackFormat(9), resourcePackFormat = PackFormat(8),
            structures = "structures",
            loaders = LoaderProfiles(
                forge = ForgeProfile(version = "40.3.12", names = ForgeNames.SRG, tests = TestRegistration.FORGE_CONTAINER,
                    launchJvmArgs = listOf("--add-exports=cpw.mods.bootstraplauncher/cpw.mods.bootstraplauncher=ALL-UNNAMED")),
                fabric = FabricProfile(version = "0.16.7", tests = TestRegistration.FABRIC_VANILLA,
                    fabricApi = "0.77.0+1.18.2", clothConfig = "6.5.102"),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.19.2", java = 17),
            compatibilityUpperBound = "1.19.3",
            resources = MinecraftResources(
                recipes = RecipeFormat(
                    directory = "recipes", ingredients = IngredientEncoding.ITEM_OBJECT,
                    craftingResult = ResultEncoding.ITEM_FIELD, upgrade = UpgradeRecipeMethod.CraftingTable,
                ),
                itemModels = ItemModelFormat.PREDICATE_OVERRIDES,
                armorTags = ArmorTagFormat.NONE, swordTags = false,
            ),
            dataPackFormat = PackFormat(10), resourcePackFormat = PackFormat(9),
            structures = "structures",
            loaders = LoaderProfiles(
                forge = ForgeProfile(version = "43.5.2", names = ForgeNames.SRG, tests = TestRegistration.FORGE_CONTAINER),
                fabric = FabricProfile(version = "0.16.7", tests = TestRegistration.FABRIC_VANILLA,
                    fabricApi = "0.77.0+1.19.2", clothConfig = "8.3.134"),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.19.3", java = 17),
            compatibilityUpperBound = "1.19.4",
            resources = MinecraftResources(
                recipes = RecipeFormat(
                    directory = "recipes", ingredients = IngredientEncoding.ITEM_OBJECT,
                    craftingResult = ResultEncoding.ITEM_FIELD, upgrade = UpgradeRecipeMethod.CraftingTable,
                ),
                itemModels = ItemModelFormat.PREDICATE_OVERRIDES,
                armorTags = ArmorTagFormat.NONE, swordTags = false,
            ),
            dataPackFormat = PackFormat(10), resourcePackFormat = PackFormat(12),
            structures = "structures",
            loaders = LoaderProfiles(
                forge = ForgeProfile(version = "44.1.23", names = ForgeNames.SRG, tests = TestRegistration.FORGE_CONTAINER),
                fabric = FabricProfile(version = "0.16.7", tests = TestRegistration.FABRIC_VANILLA,
                    fabricApi = "0.76.1+1.19.3", clothConfig = "9.1.104"),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.19.4", java = 17),
            compatibilityUpperBound = "1.19.5",
            resources = MinecraftResources(
                recipes = RecipeFormat(
                    directory = "recipes",
                    ingredients = IngredientEncoding.ITEM_OBJECT,
                    craftingResult = ResultEncoding.ITEM_FIELD,
                    upgrade = UpgradeRecipeMethod.CraftingTable,
                ),
                itemModels = ItemModelFormat.PREDICATE_OVERRIDES,
                armorTags = ArmorTagFormat.NONE,
                swordTags = false,
            ),
            dataPackFormat = PackFormat(major = 12),
            resourcePackFormat = PackFormat(major = 13),
            structures = "structures",
            loaders = LoaderProfiles(
                forge = ForgeProfile(
                    version = "45.3.9",
                    tests = TestRegistration.FORGE_CONTAINER,
                    geckolib = "4.2",
                    names = ForgeNames.SRG,
                ),
                fabric = FabricProfile(
                    version = "0.16.7",
                    tests = TestRegistration.FABRIC_VANILLA,
                    geckolib = "4.2",
                    fabricApi = "0.75.3+1.19.4",
                    clothConfig = "10.0.96",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.20.1", java = 17),
            compatibilityUpperBound = "1.20.2",
            resources = MinecraftResources(
                recipes = RecipeFormat(
                    directory = "recipes",
                    ingredients = IngredientEncoding.ITEM_OBJECT,
                    craftingResult = ResultEncoding.ITEM_FIELD,
                    upgrade = UpgradeRecipeMethod.SmithingTransform(ResultEncoding.ITEM_FIELD),
                ),
                itemModels = ItemModelFormat.PREDICATE_OVERRIDES,
                armorTags = ArmorTagFormat.NONE,
                swordTags = false,
            ),
            dataPackFormat = PackFormat(major = 15),
            resourcePackFormat = PackFormat(major = 15),
            structures = "structures",
            loaders = LoaderProfiles(
                forge = ForgeProfile(
                    version = "47.3.12",
                    tests = TestRegistration.FORGE_CONTAINER,
                    geckolib = "4.4.7",
                    names = ForgeNames.SRG,
                ),
                fabric = FabricProfile(
                    version = "0.16.7",
                    tests = TestRegistration.FABRIC_VANILLA,
                    geckolib = "4.4.7",
                    fabricApi = "0.89.0+1.20.1",
                    clothConfig = "11.0.99",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.21.1", java = 21),
            compatibilityUpperBound = "1.21.2",
            resources = MinecraftResources(
                recipes = RecipeFormat(
                    directory = "recipe",
                    ingredients = IngredientEncoding.ITEM_OBJECT,
                    craftingResult = ResultEncoding.ID_FIELD,
                    upgrade = UpgradeRecipeMethod.SmithingTransform(ResultEncoding.ID_FIELD),
                ),
                itemModels = ItemModelFormat.PREDICATE_OVERRIDES,
                armorTags = ArmorTagFormat.DYEABLE_AND_ENCHANTABLE,
                swordTags = true,
            ),
            dataPackFormat = PackFormat(major = 48),
            resourcePackFormat = PackFormat(major = 34),
            structures = "structure",
            loaders = LoaderProfiles(
                forge = ForgeProfile(
                    version = "52.1.3",
                    tests = TestRegistration.FORGE_COMPONENTS,
                    geckolib = "4.7.7",
                ),
                neoforge = NeoForgeProfile(
                    version = "21.1.206",
                    tests = TestRegistration.NEOFORGE,
                    geckolib = "4.7.7",
                ),
                fabric = FabricProfile(
                    version = "0.18.2",
                    tests = TestRegistration.FABRIC_VANILLA,
                    geckolib = "4.8.2",
                    fabricApi = "0.102.0+1.21.1",
                    clothConfig = "15.0.130",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.21.10", java = 21),
            compatibilityUpperBound = "1.21.11",
            resources = itemDefinitionResources,
            dataPackFormat = PackFormat(major = 88),
            resourcePackFormat = PackFormat(major = 69),
            structures = "structure",
            loaders = LoaderProfiles(
                forge = ForgeProfile(
                    version = "60.1.0",
                    tests = TestRegistration.FORGE_FUNCTIONS,
                    geckolib = "5.3-alpha-3",
                    classicLaunch = false,
                    testServerConfigDirectory = "gametestserver/gametestworld/serverconfig",
                ),
                neoforge = NeoForgeProfile(
                    version = "21.10.64",
                    tests = TestRegistration.NEOFORGE_FUNCTIONS,
                    geckolib = "5.3-alpha-3",
                ),
                fabric = FabricProfile(
                    version = "0.18.4",
                    tests = TestRegistration.FABRIC_REGISTRY,
                    geckolib = "5.3-alpha-3",
                    fabricApi = "0.134.1+1.21.10",
                    clothConfig = "20.0.148",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.21.11", java = 21),
            compatibilityUpperBound = "1.21.12",
            resources = itemDefinitionResources,
            dataPackFormat = PackFormat(major = 94, minor = 1),
            resourcePackFormat = PackFormat(major = 75),
            structures = "structure",
            loaders = LoaderProfiles(
                forge = ForgeProfile(
                    version = "61.2.0",
                    tests = TestRegistration.FORGE_FUNCTIONS,
                    geckolib = "5.4.5",
                    classicLaunch = false,
                    testServerConfigDirectory = "gametestserver/gametestworld/serverconfig",
                ),
                neoforge = NeoForgeProfile(
                    version = "21.11.48",
                    tests = TestRegistration.NEOFORGE_FUNCTIONS,
                    geckolib = "5.4.5",
                ),
                fabric = FabricProfile(
                    version = "0.18.4",
                    tests = TestRegistration.FABRIC_REGISTRY,
                    geckolib = "5.4.5",
                    fabricApi = "0.139.4+1.21.11",
                    clothConfig = "21.11.150",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "26.1.2", java = 25),
            compatibilityUpperBound = "26.2",
            obfuscated = false,
            resources = itemDefinitionResources.copy(recipes = itemDefinitionResources.recipes.copy(
                upgrade = UpgradeRecipeMethod.SmithingTransform(ResultEncoding.ID_FIELD),
                explicitDyeRecipes = true,
            ), armorTags = ArmorTagFormat.CAULDRON_AND_ENCHANTABLE),
            dataPackFormat = PackFormat(101, 1),
            resourcePackFormat = PackFormat(84, 0),
            structures = "structure",
            loaders = LoaderProfiles(
                forge = ForgeProfile(
                    version = "64.1.5", tests = TestRegistration.FORGE_FUNCTIONS, geckolib = "5.5.2",
                    classicLaunch = false,
                    testServerConfigDirectory = "gametestserver/gametestworld/serverconfig",
                ),
                neoforge = NeoForgeProfile(version = "26.1.2.122", tests = TestRegistration.NEOFORGE_FUNCTIONS, geckolib = "5.5.2"),
                fabric = FabricProfile(
                    version = "0.20.1", tests = TestRegistration.FABRIC_REGISTRY, geckolib = "5.5.2",
                    fabricApi = "0.145.4+26.1.2", clothConfig = "26.1.154",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "26.2", java = 25),
            compatibilityUpperBound = "26.3",
            obfuscated = false,
            resources = itemDefinitionResources.copy(recipes = itemDefinitionResources.recipes.copy(
                upgrade = UpgradeRecipeMethod.SmithingTransform(ResultEncoding.ID_FIELD),
                explicitDyeRecipes = true,
            ), armorTags = ArmorTagFormat.CAULDRON_AND_ENCHANTABLE),
            dataPackFormat = PackFormat(107, 1),
            resourcePackFormat = PackFormat(88, 0),
            structures = "structure",
            loaders = LoaderProfiles(
                forge = ForgeProfile(
                    version = "65.1.5", tests = TestRegistration.FORGE_FUNCTIONS, geckolib = "5.5.6",
                    classicLaunch = false,
                    testServerConfigDirectory = "gametestserver/gametestworld/serverconfig",
                ),
                neoforge = NeoForgeProfile(version = "26.2.0.93", tests = TestRegistration.NEOFORGE_FUNCTIONS, geckolib = "5.5.6"),
                fabric = FabricProfile(
                    version = "0.20.1", tests = TestRegistration.FABRIC_REGISTRY, geckolib = "5.5.5",
                    fabricApi = "0.152.1+26.2", clothConfig = "26.2.155",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "26.3", java = 25),
            compatibilityUpperBound = "26.4",
            obfuscated = false,
            resources = itemDefinitionResources.copy(recipes = itemDefinitionResources.recipes.copy(
                upgrade = UpgradeRecipeMethod.SmithingTransform(ResultEncoding.ID_FIELD),
                explicitDyeRecipes = true,
            ), armorTags = ArmorTagFormat.CAULDRON_AND_ENCHANTABLE),
            dataPackFormat = PackFormat(121, 0),
            resourcePackFormat = PackFormat(97, 1),
            structures = "structure",
            loaders = LoaderProfiles(
                forge = ForgeProfile(
                    version = "66.1.0", tests = TestRegistration.FORGE_FUNCTIONS, geckolib = "5.5.7",
                    classicLaunch = false,
                    testServerConfigDirectory = "gametestserver/gametestworld/serverconfig",
                ),
                neoforge = NeoForgeProfile(version = "26.3.0.95", tests = TestRegistration.NEOFORGE_FUNCTIONS, geckolib = "5.5.7"),
                fabric = FabricProfile(
                    version = "0.19.3", tests = TestRegistration.FABRIC_REGISTRY, geckolib = "5.5.7",
                    fabricApi = "0.160.5+26.3", clothConfig = "26.3.158",
                ),
            ),
        ),
    ).associateBy { it.runtime.id }

    operator fun get(id: String): MinecraftVersion =
        requireNotNull(versions[id]) { "Unverified Minecraft version: $id" }

    val supported: Set<String> get() = versions.keys
}
