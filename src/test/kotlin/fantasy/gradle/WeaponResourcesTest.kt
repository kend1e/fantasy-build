package fantasy.gradle

import com.google.gson.JsonParser
import fantasy.gradle.content.ShapedRecipe
import fantasy.gradle.content.weapons.contentCatalog
import fantasy.gradle.content.weapons.contentResources
import fantasy.gradle.resources.translations
import org.junit.Assert.*
import org.junit.Test

class WeaponResourcesTest {
    private val content = contentCatalog(
        namespace = "example",
        tabIcon = "sword",
        base = "minecraft:netherite_sword",
        addition = "example:crystal",
        tab = "weapons",
    ) {
        material("crystal", ShapedRecipe(listOf("X"), mapOf('X' to "minecraft:stone")))
        weapon("sword", "minecraft:gold_nugget")
    }
    private val languages = translations("example", tab = "weapons") {
        language("en_us") {
            tabTitle("Weapons")
            item("crystal", "Crystal")
            item("sword", "Sword")
        }
    }

    @Test fun `all weapon and material recipes use the Minecraft 1_21_1 format`() {
        val files = contentResources(content, languages, "1.21.1")
        assertFalse(files.keys.any { "/recipes/" in it })
        for (id in content.itemIds) {
            val recipe = JsonParser.parseString(files.getValue("data/example/recipe/$id.json")).asJsonObject
            assertEquals("example:$id", recipe.getAsJsonObject("result").get("id").asString)
            assertFalse(recipe.getAsJsonObject("result").has("item"))
        }
    }

    @Test fun `modern item definitions select large models only in hand contexts`() {
        for (version in listOf("1.21.10", "1.21.11")) {
            val files = contentResources(content, languages, version)
            val model = JsonParser.parseString(files.getValue("assets/example/items/sword.json")).asJsonObject.getAsJsonObject("model")
            assertEquals("minecraft:select", model.get("type").asString)
            assertEquals("minecraft:display_context", model.get("property").asString)
            val case = model.getAsJsonArray("cases").single().asJsonObject
            assertEquals(setOf("firstperson_lefthand", "firstperson_righthand", "thirdperson_lefthand", "thirdperson_righthand"),
                case.getAsJsonArray("when").map { it.asString }.toSet())
            assertEquals("example:item/sword_large", case.getAsJsonObject("model").get("model").asString)
            assertEquals("example:item/sword", model.getAsJsonObject("fallback").get("model").asString)
            val material = JsonParser.parseString(files.getValue("assets/example/items/crystal.json")).asJsonObject.getAsJsonObject("model")
            assertEquals("example:item/crystal", material.get("model").asString)
            val plain = content.copy(weapons = content.weapons.map { it.copy(largeModel = null) })
            val plainFiles = contentResources(plain, languages, version)
            val plainModel = JsonParser.parseString(plainFiles.getValue("assets/example/items/sword.json")).asJsonObject.getAsJsonObject("model")
            assertEquals("minecraft:model", plainModel.get("type").asString)
            assertFalse(plainFiles.containsKey("assets/example/models/item/sword_large.json"))
        }
        for (version in listOf("1.19.4", "1.20.1", "1.21.1")) {
            assertFalse(contentResources(content, languages, version).keys.any { "/items/" in it })
        }
    }

    @Test fun `duplicate items and invalid patterns are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            contentCatalog("example", "sword", "minecraft:stick", "minecraft:stone", tab = "weapons") {
                weapon("sword", "minecraft:coal")
                weapon("sword", "minecraft:coal")
            }
        }
        assertThrows(IllegalArgumentException::class.java) {
            ShapedRecipe(listOf("XX"), mapOf('Y' to "minecraft:stone"))
        }
    }

    @Test fun `missing translations and unverified Minecraft versions are rejected`() {
        val missingNames = translations("example", tab = "weapons") {
            language("en_us") { tabTitle("Weapons") }
        }
        assertThrows(IllegalArgumentException::class.java) {
            contentResources(content, missingNames, "1.20.1")
        }
        assertThrows(IllegalArgumentException::class.java) {
            contentResources(content, languages, "1.22")
        }
    }
}
