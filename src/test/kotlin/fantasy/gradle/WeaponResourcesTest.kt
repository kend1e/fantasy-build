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
