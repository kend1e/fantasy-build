import fantasy.gradle.*
import fantasy.gradle.Target
import net.minecraftforge.renamer.gradle.RenameJar

plugins {
    id("net.minecraftforge.gradle")
    id("net.minecraftforge.renamer")
    java
}

val target = extensions.getByType<Target>()
require(target.loader == Loader.FORGE && target.build.forgeNames == ForgeNames.SRG) {
    "The SRG adapter requires a Forge target with SRG names"
}
dependencies {
    target.dependencies.filterIsInstance<GeckoLib>().forEach { implementation(renamer.dependency(it.notation(target))) }
}
renamer.mappings.from(minecraft.getDependency(project.name).toSrgFile)
renamer.classes(tasks.jar) { archiveClassifier.set("srg") }
val devRefmapMappings = renamer.convert(
    "devRefmapMappings", minecraft.getDependency(project.name).toSrgFile, "srg"
) { reverse.set(true) }
minecraft.runs.configureEach {
    systemProperty("mixin.env.remapRefMap", "true")
    systemProperty("mixin.env.refMapRemappingFile",
        devRefmapMappings.flatMap { it.output }.map { it.asFile.absolutePath })
}
tasks.matching { it.name in setOf("runClient", "runServer", "runTestGameTestServer") }.configureEach {
    dependsOn(devRefmapMappings)
}
registerReleaseExport(tasks.named<RenameJar>("renameJar").flatMap { it.output })
