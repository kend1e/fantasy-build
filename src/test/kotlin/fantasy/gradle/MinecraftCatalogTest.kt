package fantasy.gradle

import com.google.gson.JsonParser
import fantasy.gradle.minecraft.*
import fantasy.gradle.resources.*
import fantasy.gradle.testing.*
import org.junit.Assert.*
import org.junit.Test

class MinecraftCatalogTest {
    @Test fun `catalog supplies defaults without adding optional libraries`() {
        val targets = buildTargets("1") {
            forge("1.20.1", version = "47.3.0")
            neoforge("1.21.1")
            fabric("1.21.1") { fabricApi(); clothConfig() }
        }
        assertEquals("47.3.0", targets[0].loaderVersion)
        assertEquals(ForgeNames.SRG, targets[0].build.forgeNames)
        assertEquals(listOf(17, 21, 21), targets.map { it.java })
        assertTrue(targets.take(2).all { it.dependencies.isEmpty() })
        assertEquals(setOf("fabric-api", "cloth-config2"), targets.last().dependencies.map { it.id }.toSet())
        assertThrows(IllegalArgumentException::class.java) { buildTargets("1") { forge("1.22") } }
        assertThrows(IllegalArgumentException::class.java) { buildTargets("1") { neoforge("1.20.1") } }
    }

    @Test fun `upgrade recipes follow the actual Minecraft schemas`() {
        fun recipe(version: String) = JsonParser.parseString(upgradeRecipe("example:template", "minecraft:netherite_helmet",
            "example:crystal", "example:helmet", MinecraftVersions[version].resources.recipes)).asJsonObject
        val crafting = recipe("1.19.4")
        assertEquals("minecraft:crafting_shapeless", crafting["type"].asString)
        assertEquals(3, crafting["ingredients"].asJsonArray.size())
        assertEquals("example:helmet", crafting["result"].asJsonObject["item"].asString)
        assertEquals("example:helmet", recipe("1.20.1")["result"].asJsonObject["item"].asString)
        assertEquals("example:helmet", recipe("1.21.1")["result"].asJsonObject["id"].asString)
        for (version in listOf("1.21.10", "1.21.11")) {
            assertEquals("example:helmet", recipe(version)["result"].asString)
            assertEquals("example:template", recipe(version)["template"].asString)
            val shaped = JsonParser.parseString(shapedRecipe(ShapedRecipeDefinition("material", listOf("X"),
                mapOf("X" to "minecraft:stone"), "example:material", 2), MinecraftVersions[version].resources.recipes)).asJsonObject
            assertEquals("minecraft:stone", shaped["key"].asJsonObject["X"].asString)
            assertEquals(2, shaped["result"].asJsonObject["count"].asInt)
        }
    }

    @Test fun `pack metadata supports both resource and data formats`() {
        val pack = JsonParser.parseString(MinecraftVersions["1.21.1"].packMetadata("Example")).asJsonObject["pack"].asJsonObject
        assertEquals(48, pack["pack_format"].asInt)
        assertEquals(34, pack["supported_formats"].asJsonObject["min_inclusive"].asInt)
        val modern = JsonParser.parseString(MinecraftVersions["1.21.11"].packMetadata("Example", dataOnly = true)).asJsonObject["pack"].asJsonObject
        assertEquals(listOf(94, 1), modern["min_format"].asJsonArray.map { it.asInt })
        assertFalse(modern.has("supported_formats"))
    }

    @Test fun `test registration uses the declared module in every generation`() {
        val module = TestModule("example_tests", "org.example.test", "ExampleTests", "example.report")
        val cases = listOf(TestCase("recipes", "RecipeTests"))
        for (registration in TestRegistration.entries) {
            val source = registration.entrypoint(cases, module)
            assertTrue(source.contains("package org.example.test;"))
            assertTrue(source.contains("class ExampleTests"))
            assertFalse(source.contains("fantasyarmor"))
            assertFalse(source.contains("fantasyweapons"))
        }
        assertEquals("example_tests:example_tests_recipes", TestRegistration.FABRIC_REGISTRY.reportName("recipes", module))
        assertEquals("exampletests.recipes", TestRegistration.FABRIC_VANILLA.reportName("recipes", module))
    }

    @Test fun `translation keys come from content configuration`() {
        val catalog = translations("example", tab = "equipment") { language("en_us") { tabTitle("Equipment"); item("sword", "Sword") } }
        catalog.validateItems(setOf("sword"), "itemGroup.example.equipment")
        assertTrue(catalog.resources().containsKey("assets/example/lang/en_us.json"))
        assertThrows(IllegalArgumentException::class.java) { catalog.validateItems(setOf("other"), "itemGroup.example.equipment") }
        assertThrows(IllegalArgumentException::class.java) { translations("example") { language("en_us") { item("sword", "Sword"); item("sword", "Again") } } }
    }
}
