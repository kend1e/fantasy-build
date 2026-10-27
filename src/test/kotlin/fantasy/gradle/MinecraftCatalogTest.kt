package fantasy.gradle

import com.google.gson.JsonParser
import fantasy.gradle.minecraft.*
import fantasy.gradle.resources.*
import fantasy.gradle.testing.*
import org.junit.Assert.*
import org.junit.Test

class MinecraftCatalogTest {
    @Test fun `Minecraft metadata stops before incompatible updates for every supported target`() {
        val bounds = mapOf(
            "1.12.2" to "1.13", "1.14.4" to "1.15", "1.15.2" to "1.16", "1.16.5" to "1.17",
            "1.18.2" to "1.19", "1.19.2" to "1.19.3", "1.19.3" to "1.19.4", "1.19.4" to "1.19.5", "1.20.1" to "1.20.2", "1.21.1" to "1.21.2",
            "1.21.10" to "1.21.11", "1.21.11" to "1.21.12",
            "26.1.2" to "26.2", "26.2" to "26.3", "26.3" to "26.4",
        )
        val metadata = modMetadata {
            id = "example"
            name = "Example"
            group = "org.example"
            license = "MIT"
        }
        assertEquals(MinecraftVersions.supported, bounds.keys)
        bounds.forEach { (minecraft, upper) ->
            val version = MinecraftVersions[minecraft]
            val targets = buildTargets("1") {
                if (version.loaders.forge != null) forge(minecraft)
                if (version.loaders.neoforge != null) neoforge(minecraft)
                if (version.loaders.fabric != null) fabric(minecraft)
            }
            targets.forEach { target ->
                if (target.loader == Loader.FABRIC) {
                    val depends = metadata.fabricValues(target)["depends"] as Map<*, *>
                    assertEquals(">=$minecraft <$upper-", depends["minecraft"])
                } else {
                    assertEquals("[$minecraft,$upper)", metadata.templateValues(target)["minecraft_version_range"])
                }
            }
        }
    }

    @Test fun `item tooltips are accepted only for declared items`() {
        fun catalog(tooltipId: String) = translations("example", tab = "equipment") {
            language("en_us") {
                tabTitle("Equipment")
                item("crystal", "Crystal")
                entry("item.example.$tooltipId.tooltip", "A mysterious crystal.")
            }
        }
        catalog("crystal").validateItems(setOf("crystal"), "itemGroup.example.equipment")
        assertThrows(IllegalArgumentException::class.java) {
            catalog("cristal").validateItems(setOf("crystal"), "itemGroup.example.equipment")
        }
    }

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

    @Test fun `pack metadata retains every verified version format`() {
        val expected = mapOf(
            "1.12.2" to """{"pack":{"description":"Example","pack_format":3}}""",
            "1.14.4" to """{"pack":{"description":"Example","pack_format":4}}""",
            "1.15.2" to """{"pack":{"description":"Example","pack_format":5}}""",
            "1.16.5" to """{"pack":{"description":"Example","pack_format":6}}""",
            "1.18.2" to """{"pack":{"description":"Example","pack_format":9,"supported_formats":{"min_inclusive":8,"max_inclusive":9}}}""",
            "1.19.2" to """{"pack":{"description":"Example","pack_format":10,"supported_formats":{"min_inclusive":9,"max_inclusive":10}}}""",
            "1.19.3" to """{"pack":{"description":"Example","pack_format":10,"supported_formats":{"min_inclusive":10,"max_inclusive":12}}}""",
            "1.19.4" to """{"pack":{"description":"Example","pack_format":12,"supported_formats":{"min_inclusive":12,"max_inclusive":13}}}""",
            "1.20.1" to """{"pack":{"description":"Example","pack_format":15}}""",
            "1.21.1" to """{"pack":{"description":"Example","pack_format":48,"supported_formats":{"min_inclusive":34,"max_inclusive":48}}}""",
            "1.21.10" to """{"pack":{"description":"Example","pack_format":88,"min_format":88,"max_format":88}}""",
            "1.21.11" to """{"pack":{"description":"Example","pack_format":94,"min_format":[94,1],"max_format":94}}""",
            "26.1.2" to """{"pack":{"description":"Example","pack_format":101,"min_format":[101,1],"max_format":101}}""",
            "26.2" to """{"pack":{"description":"Example","pack_format":107,"min_format":[107,1],"max_format":107}}""",
            "26.3" to """{"pack":{"description":"Example","pack_format":121,"min_format":121,"max_format":121}}""",
        )
        assertEquals(MinecraftVersions.supported, expected.keys)
        expected.forEach { (id, json) ->
            assertEquals(id, JsonParser.parseString(json), JsonParser.parseString(MinecraftVersions[id].packMetadata("Example")))
            val dataOnly = JsonParser.parseString(json).asJsonObject.apply { getAsJsonObject("pack").remove("supported_formats") }
            if (MinecraftVersions[id].dataPackFormat == null) {
                assertThrows(IllegalArgumentException::class.java) { MinecraftVersions[id].packMetadata("Example", dataOnly = true) }
            } else assertEquals(id, dataOnly, JsonParser.parseString(MinecraftVersions[id].packMetadata("Example", dataOnly = true)))
        }
    }

    @Test fun `typed loader access rejects unsupported combinations even with explicit versions`() {
        val version = MinecraftVersions["1.21.1"]
        assertSame(version.forge, version.loader(Loader.FORGE))
        assertSame(version.neoforge, version.loader(Loader.NEOFORGE))
        assertSame(version.fabric, version.loader(Loader.FABRIC))
        assertThrows(IllegalArgumentException::class.java) { buildTargets("1") { neoforge("1.20.1", "21.1.206") } }
        assertThrows(IllegalArgumentException::class.java) { buildTargets("1") { neoforge("1.19.4", "21.1.206") } }
        assertThrows(IllegalArgumentException::class.java) { buildTargets("1") { forge("1.22", "61.2.0") } }
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
