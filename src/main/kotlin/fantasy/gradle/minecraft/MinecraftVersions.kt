package fantasy.gradle.minecraft

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
            runtime = MinecraftRuntime(id = "1.19.4", java = 17),
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
                    fabricApi = "0.87.2+1.19.4",
                    clothConfig = "10.1.135",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.20.1", java = 17),
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
                    fabricApi = "0.92.2+1.20.1",
                    clothConfig = "11.1.136",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.21.1", java = 21),
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
                    fabricApi = "0.116.7+1.21.1",
                    clothConfig = "15.0.140",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.21.10", java = 21),
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
                    fabricApi = "0.138.4+1.21.10",
                    clothConfig = "20.0.149",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "1.21.11", java = 21),
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
                    fabricApi = "0.141.6+1.21.11",
                    clothConfig = "21.11.160",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "26.1.2", java = 25),
            obfuscated = false,
            resources = itemDefinitionResources.copy(recipes = itemDefinitionResources.recipes.copy(
                upgrade = UpgradeRecipeMethod.SmithingTransform(ResultEncoding.ID_FIELD),
            )),
            dataPackFormat = PackFormat(101, 1),
            resourcePackFormat = PackFormat(84, 0),
            structures = "structure",
            loaders = LoaderProfiles(
                forge = ForgeProfile(
                    version = "64.1.5", tests = TestRegistration.FORGE_FUNCTIONS,
                    classicLaunch = false,
                    testServerConfigDirectory = "gametestserver/gametestworld/serverconfig",
                ),
                neoforge = NeoForgeProfile(version = "26.1.2.122", tests = TestRegistration.NEOFORGE_FUNCTIONS),
                fabric = FabricProfile(
                    version = "0.20.1", tests = TestRegistration.FABRIC_REGISTRY,
                    fabricApi = "0.155.3+26.1.2", clothConfig = "26.1.154",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "26.2", java = 25),
            obfuscated = false,
            resources = itemDefinitionResources.copy(recipes = itemDefinitionResources.recipes.copy(
                upgrade = UpgradeRecipeMethod.SmithingTransform(ResultEncoding.ID_FIELD),
            )),
            dataPackFormat = PackFormat(107, 1),
            resourcePackFormat = PackFormat(88, 0),
            structures = "structure",
            loaders = LoaderProfiles(
                forge = ForgeProfile(
                    version = "65.1.5", tests = TestRegistration.FORGE_FUNCTIONS,
                    classicLaunch = false,
                    testServerConfigDirectory = "gametestserver/gametestworld/serverconfig",
                ),
                neoforge = NeoForgeProfile(version = "26.2.0.93", tests = TestRegistration.NEOFORGE_FUNCTIONS),
                fabric = FabricProfile(
                    version = "0.20.1", tests = TestRegistration.FABRIC_REGISTRY,
                    fabricApi = "0.161.0+26.2", clothConfig = "26.2.155",
                ),
            ),
        ),
        MinecraftVersion(
            runtime = MinecraftRuntime(id = "26.3", java = 25),
            obfuscated = false,
            resources = itemDefinitionResources.copy(recipes = itemDefinitionResources.recipes.copy(
                upgrade = UpgradeRecipeMethod.SmithingTransform(ResultEncoding.ID_FIELD),
            )),
            dataPackFormat = PackFormat(121, 0),
            resourcePackFormat = PackFormat(97, 1),
            structures = "structure",
            loaders = LoaderProfiles(
                forge = ForgeProfile(
                    version = "66.1.0", tests = TestRegistration.FORGE_FUNCTIONS,
                    classicLaunch = false,
                    testServerConfigDirectory = "gametestserver/gametestworld/serverconfig",
                ),
                neoforge = NeoForgeProfile(version = "26.3.0.95", tests = TestRegistration.NEOFORGE_FUNCTIONS),
                fabric = FabricProfile(
                    version = "0.20.1", tests = TestRegistration.FABRIC_REGISTRY,
                    fabricApi = "0.165.0+26.3", clothConfig = "26.3.159",
                ),
            ),
        ),
    ).associateBy { it.runtime.id }

    operator fun get(id: String): MinecraftVersion =
        requireNotNull(versions[id]) { "Unverified Minecraft version: $id" }

    val supported: Set<String> get() = versions.keys
}
