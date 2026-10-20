package fantasy.gradle

import java.io.Serializable
import fantasy.gradle.minecraft.MinecraftVersions

@DslMarker
annotation class ModBuildDsl

data class MinecraftRuntime(val id: String, val java: Int) : Serializable {
    init {
        require(id.matches(Regex("[A-Za-z0-9][A-Za-z0-9._-]*"))) { "Invalid Minecraft version: $id" }
        require(java > 0) { "$id: Java version must be positive" }
    }
}

data class Target(
    val settings: LoaderSettings,
    val minecraftVersion: MinecraftRuntime,
    val releaseVersion: String,
    val dependencies: List<ModDependency>,
    val minecraftRange: String,
    val build: LoaderBuild = LoaderBuild(),
) : Serializable {
    val loader get() = settings.loader
    val loaderVersion get() = settings.version
    val minecraft get() = minecraftVersion.id
    val java get() = minecraftVersion.java
    val modVersion get() = "$releaseVersion-$minecraft"
    val name get() = "${loader.id}-$minecraft"
    val path get() = ":platforms:$name"
    val loaderRange get() = loader.languageVersion(loaderVersion)
    val loaderDependencyRange get() = loader.minimumVersion(loaderVersion)

    init {
        require(loaderVersion.isNotBlank()) { "$name: loader version is required" }
        require(releaseVersion.isNotBlank()) { "$name: release version is required" }
        require(minecraftRange.isNotBlank()) { "$name: Minecraft version range is required" }
        require(loader == Loader.FORGE || build.forgeNames == ForgeNames.OFFICIAL) { "$name: SRG mappings are supported only by the Forge adapter" }
        require(dependencies.map { it.id }.distinct().size == dependencies.size) { "$name: duplicate mod dependencies" }
        dependencies.forEach { dependency ->
            val pattern = if (loader == Loader.FABRIC) "[a-z][a-z0-9_-]{1,63}" else "[a-z][a-z0-9_]{1,63}"
            require(dependency.id.matches(Regex(pattern))) { "$name: invalid dependency ID: ${dependency.id}" }
            require(dependency.id !in setOf("minecraft", "java", "forge", "neoforge", "fabricloader")) {
                "$name: platform dependency '${dependency.id}' must not be overridden"
            }
            require(dependency.version.isNotBlank()) { "$name: ${dependency.id} version is required" }
            dependency.validate(loader)
        }
    }

    inline fun <reified T : ModDependency> dependency(): T = dependencies.filterIsInstance<T>().singleOrNull()
        ?: error("$name: declare ${T::class.simpleName} in the target dependencies")

    fun releaseFileName(metadata: ModMetadata) = "${metadata.releaseName}-$releaseVersion-$minecraft-${loader.id}.jar"
}

@ModBuildDsl
open class TargetBuilder internal constructor(
    protected val runtime: MinecraftRuntime,
    protected val settings: LoaderSettings,
    releaseVersion: String,
) {
    private val dependencies = mutableListOf<ModDependency>()
    var releaseVersion: String = releaseVersion
    var minecraftRange: String = settings.loader.minimumVersion(runtime.id)
    var dataGeneration: Boolean = false

    fun dependency(dependency: ModDependency) { dependencies.add(dependency) }
    fun requiredMod(id: String, version: String) = dependency(RequiredMod(id, version))
    fun geckolib(version: String = MinecraftVersions[runtime.id].loader(settings.loader).geckolib) = dependency(GeckoLib(version))

    internal open fun build() = Target(settings, runtime, releaseVersion, dependencies.toList(), minecraftRange, LoaderBuild(dataGeneration))
}

@ModBuildDsl
class ForgeTargetBuilder internal constructor(runtime: MinecraftRuntime, version: String, releaseVersion: String) :
    TargetBuilder(runtime, ForgeSettings(version), releaseVersion) {
    var names: ForgeNames = ForgeNames.OFFICIAL
    internal override fun build() = super.build().let { it.copy(build = it.build.copy(forgeNames = names)) }
}

@ModBuildDsl
class FabricTargetBuilder internal constructor(runtime: MinecraftRuntime, version: String, releaseVersion: String) :
    TargetBuilder(runtime, FabricSettings(version), releaseVersion) {
    fun fabricApi(version: String = MinecraftVersions[runtime.id].fabric.fabricApi) = dependency(FabricApi(version))
    fun clothConfig(version: String = MinecraftVersions[runtime.id].fabric.clothConfig) = dependency(ClothConfig(version))
}

@ModBuildDsl
class TargetsBuilder internal constructor(private val releaseVersion: String) {
    private val targets = linkedMapOf<String, Target>()

    fun forge(runtime: MinecraftRuntime, version: String, configure: ForgeTargetBuilder.() -> Unit = {}) =
        add(ForgeTargetBuilder(runtime, version, releaseVersion).apply(configure).build())

    fun neoforge(runtime: MinecraftRuntime, version: String, configure: TargetBuilder.() -> Unit = {}) =
        add(TargetBuilder(runtime, NeoForgeSettings(version), releaseVersion).apply(configure).build())

    fun fabric(runtime: MinecraftRuntime, version: String, configure: FabricTargetBuilder.() -> Unit = {}) =
        add(FabricTargetBuilder(runtime, version, releaseVersion).apply(configure).build())

    fun forge(minecraft: String, version: String = MinecraftVersions[minecraft].loader(Loader.FORGE).version,
              configure: ForgeTargetBuilder.() -> Unit = {}) = forge(MinecraftVersions[minecraft].runtime, version) {
        names = MinecraftVersions[minecraft].forge.names
        configure()
    }

    fun neoforge(minecraft: String, version: String = MinecraftVersions[minecraft].loader(Loader.NEOFORGE).version,
                 configure: TargetBuilder.() -> Unit = {}) = neoforge(MinecraftVersions[minecraft].runtime, version, configure)

    fun fabric(minecraft: String, version: String = MinecraftVersions[minecraft].loader(Loader.FABRIC).version,
               configure: FabricTargetBuilder.() -> Unit = {}) = fabric(MinecraftVersions[minecraft].runtime, version, configure)

    private fun add(target: Target) {
        require(targets.putIfAbsent(target.path, target) == null) { "Duplicate build target: ${target.name}" }
    }

    internal fun build(): List<Target> {
        require(targets.isNotEmpty()) { "Declare at least one build target" }
        return targets.values.toList()
    }
}

fun buildTargets(releaseVersion: String, configure: TargetsBuilder.() -> Unit): List<Target> =
    TargetsBuilder(releaseVersion).apply(configure).build()
