package fantasy.gradle

import com.google.gson.JsonParser
import org.junit.Assert.*
import org.junit.Test

class TargetDefinitionsTest {
    @Test fun `icon metadata follows loader versions without changing older targets`() {
        for (minecraft in listOf("26.1.2", "26.2", "26.3")) {
            val targets = buildTargets("1") {
                forge(minecraft)
                neoforge(minecraft)
                fabric(minecraft)
            }
            assertEquals("logoFile", metadata.templateValues(targets[0])["mod_icon_key"])
            assertEquals(if (minecraft == "26.1.2") "logoFile" else "iconFile",
                metadata.templateValues(targets[1])["mod_icon_key"])
            assertEquals("logo.png", metadata.copy(icon = "logo.png").fabricValues(targets[2])["icon"])
        }
        assertEquals("logoFile", NeoForgeSettings("21.11.42").iconMetadataKey)
        assertEquals("logoFile", NeoForgeSettings("26.2.0.49-beta").iconMetadataKey)
        assertEquals("iconFile", NeoForgeSettings("26.2.0.50-beta").iconMetadataKey)
    }

    @Test fun `loader build options are declared per target`() {
        val targets = buildTargets("1") {
            forge("1.20.1", "47.3.12") {
                names = ForgeNames.SRG
                dataGeneration = true
            }
            fabric("1.21.1", "0.18.2")
        }
        assertEquals(ForgeNames.SRG, targets.first().build.forgeNames)
        assertTrue(targets.first().build.dataGeneration)
        assertFalse(targets.last().build.dataGeneration)
        assertThrows(IllegalArgumentException::class.java) {
            targets.last().copy(build = LoaderBuild(forgeNames = ForgeNames.SRG))
        }
    }
    private val metadata = modMetadata {
        id = "fantasy_weapons"
        name = "Fantasy Weapons"
        group = "example.weapons"
        license = "All Rights Reserved"
    }

    @Test fun `targets do not require content libraries`() {
        val targets = buildTargets("0.4") {
            forge("1.21.1", "52.1.3")
            neoforge("1.21.1", "21.1.206")
            fabric("1.21.1", "0.18.2")
        }
        assertTrue(targets.all { it.dependencies.isEmpty() })
        val fabric = metadata.fabricValues(targets.last())["depends"] as Map<*, *>
        assertEquals(setOf("fabricloader", "minecraft", "java"), fabric.keys)
        targets.take(2).forEach {
            assertEquals("", metadata.templateValues(it)["mod_dependencies"])
        }
        assertEquals("fantasy-weapons-0.4-1.21.1-forge.jar", targets.first().releaseFileName(metadata))
    }

    @Test fun `declared libraries supply versions ranges and coordinates`() {
        val target = buildTargets("1.3") {
            fabric("1.21.1", "0.18.2") {
                fabricApi("0.116.7+1.21.1")
                geckolib("4.8.2")
                clothConfig("15.0.140")
                requiredMod("custom_library", "2.0")
            }
        }.single()
        val dependencies = metadata.fabricValues(target)["depends"] as Map<*, *>
        assertEquals(">=4.8.2", dependencies["geckolib"])
        assertEquals(">=15.0.140", dependencies["cloth-config2"])
        assertEquals(">=2.0", dependencies["custom_library"])
        assertEquals("software.bernie.geckolib:geckolib-fabric-1.21.1:4.8.2", target.dependency<GeckoLib>().notation(target))
        assertEquals("net.fabricmc.fabric-api:fabric-api:0.116.7+1.21.1", target.dependency<FabricApi>().notation)
        assertEquals("me.shedaniel.cloth:cloth-config-fabric:15.0.140", target.dependency<ClothConfig>().notation)
    }

    @Test fun `GeckoLib coordinates follow the published group change`() {
        val old = buildTargets("1") { fabric("1.21.11") { geckolib() } }.single()
        val modern = buildTargets("1") { fabric("26.1.2") { geckolib() } }.single()
        assertEquals("software.bernie.geckolib:geckolib-fabric-1.21.11:5.4.5", old.dependency<GeckoLib>().notation(old))
        assertEquals("com.geckolib:geckolib-fabric-26.1.2:5.5.2", modern.dependency<GeckoLib>().notation(modern))
    }

    @Test fun `duplicate targets and dependencies are rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            buildTargets("1") { forge("1.21.1", "52.1.3"); forge("1.21.1", "52.1.4") }
        }
        assertThrows(IllegalArgumentException::class.java) {
            buildTargets("1") { forge("1.21.1", "52.1.3") { geckolib("4.7.7"); requiredMod("geckolib", "4.8") } }
        }
    }

    @Test fun `Fabric integrations cannot be attached to Forge or NeoForge`() {
        for (library in listOf(FabricApi("0.116.7"), ClothConfig("15.0.140"))) {
            assertThrows(IllegalArgumentException::class.java) {
                buildTargets("1") { forge("1.21.1", "52.1.3") { dependency(library) } }
            }
            assertThrows(IllegalArgumentException::class.java) {
                buildTargets("1") { neoforge("1.21.1", "21.1.206") { dependency(library) } }
            }
        }
    }

    @Test fun `platform dependencies and empty versions cannot be overridden`() {
        assertThrows(IllegalArgumentException::class.java) {
            buildTargets("1") { fabric("1.21.1", "0.18.2") { requiredMod("minecraft", "2") } }
        }
        assertThrows(IllegalArgumentException::class.java) {
            buildTargets("1") { fabric("1.21.1", "") }
        }
        assertThrows(IllegalArgumentException::class.java) {
            buildTargets("1") { fabric("1.21.1", "0.18.2") { clothConfig("") } }
        }
        assertThrows(IllegalArgumentException::class.java) { buildTargets("1") {} }
    }

    @Test fun `metadata extends entrypoints but rejects competing identity or dependency declarations`() {
        val target = buildTargets("0.4") { fabric("1.21.1", "0.18.2") }.single()
        val template = """{"schemaVersion":1,"entrypoints":{"main":["example.Mod"]}}"""
        val result = JsonParser.parseString(renderFabricMetadata(template, metadata, target)).asJsonObject
        assertEquals("example.Mod", result.getAsJsonObject("entrypoints").getAsJsonArray("main")[0].asString)
        assertEquals("fantasy_weapons", result["id"].asString)
        assertFalse(result.has("icon"))
        assertFalse(result.has("contact"))
        for (conflict in listOf("id", "depends")) {
            assertThrows(IllegalArgumentException::class.java) {
                renderFabricMetadata("""{"schemaVersion":1,"$conflict":{}}""", metadata, target)
            }
        }
    }

    @Test fun `Minecraft compatibility can be narrowed explicitly`() {
        val target = buildTargets("0.4") {
            fabric("1.21.1", "0.18.2") { minecraftRange = "~1.21.1" }
        }.single()
        assertEquals("~1.21.1", (metadata.fabricValues(target)["depends"] as Map<*, *>)["minecraft"])
    }

    @Test fun `older targets can keep a different release version`() {
        val targets = buildTargets("1.3") {
            forge("1.20.1", "47.3.12") { releaseVersion = "1.2" }
            forge("1.21.1", "52.1.3")
        }
        assertEquals("1.2-1.20.1", targets.first().modVersion)
        assertEquals("1.3-1.21.1", targets.last().modVersion)
    }

    @Test fun `declared TOML dependencies use each loaders required field`() {
        val targets = buildTargets("1.3") {
            forge("1.21.1", "52.1.3") { geckolib("4.7.7") }
            neoforge("1.21.1", "21.1.206") { geckolib("4.7.7") }
        }
        val forge = metadata.templateValues(targets.first())["mod_dependencies"]!!
        val neo = metadata.templateValues(targets.last())["mod_dependencies"]!!
        for (text in listOf(forge, neo)) {
            assertTrue(text.contains("[[dependencies.fantasy_weapons]]"))
            assertTrue(text.contains("modId=\"geckolib\""))
            assertTrue(text.contains("versionRange=\"[4.7.7,)\""))
        }
        assertTrue(forge.contains("mandatory=true"))
        assertFalse(forge.contains("type="))
        assertTrue(neo.contains("type=\"required\""))
        assertFalse(neo.contains("mandatory="))
    }
}
