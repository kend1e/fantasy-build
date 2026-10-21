package fantasy.gradle.testing

import fantasy.gradle.Target
import fantasy.gradle.Loader

data class TestCase(
    val id: String,
    val owner: String,
    val minecraft: Set<String> = emptySet(),
    val loaders: Set<Loader> = emptySet(),
) {
    init {
        require(id.matches(Regex("[a-z][a-z0-9_]*"))) { "Invalid test id: $id" }
        require(owner.matches(Regex("[A-Z][A-Za-z0-9]*"))) { "Invalid test owner: $owner" }
    }
    fun appliesTo(target: Target) = (minecraft.isEmpty() || target.minecraft in minecraft) &&
        (loaders.isEmpty() || target.loader in loaders)
}

fun validateTestCatalog(cases: List<TestCase>, targets: List<Target>) {
    val versions = targets.map { it.minecraft }.toSet()
    for (case in cases) {
        val unknown = case.minecraft - versions
        require(unknown.isEmpty()) { "Test '${case.id}' references unsupported Minecraft versions: $unknown" }
        require(targets.any(case::appliesTo)) {
            "Test '${case.id}' matches no supported targets: minecraft=${case.minecraft}, loaders=${case.loaders}"
        }
    }
}

enum class TestRegistration {
    FORGE_CONTAINER, FORGE_COMPONENTS, NEOFORGE, FABRIC_VANILLA, FORGE_FUNCTIONS, NEOFORGE_FUNCTIONS, FABRIC_REGISTRY;

    fun annotation(module: TestModule): String = when (this) {
        FORGE_FUNCTIONS, NEOFORGE_FUNCTIONS -> error("Forge function tests do not use annotations")
        FABRIC_REGISTRY -> "@net.fabricmc.fabric.api.gametest.v1.GameTest(maxTicks = 100)"
        NEOFORGE -> "@GameTest(template = \"empty\", templateNamespace = \"${module.id}\")"
        FORGE_CONTAINER -> "@GameTest(template = \"empty\", templateNamespace = \"${module.id}\")"
        FORGE_COMPONENTS -> "@GameTest(template = \"${module.id}:empty\")"
        FABRIC_VANILLA -> "@GameTest(template = \"fabric-gametest-api-v1:empty\")"
    }

    fun holder(module: TestModule): String = when (this) {
        FORGE_CONTAINER -> "@net.minecraftforge.gametest.GameTestHolder(\"${module.id}\")\n@net.minecraftforge.gametest.PrefixGameTestTemplate(false)"
        FORGE_COMPONENTS -> "@net.minecraftforge.gametest.GameTestHolder(\"${module.id}\")"
        NEOFORGE -> "@net.neoforged.neoforge.gametest.GameTestHolder(\"${module.id}\")\n@net.neoforged.neoforge.gametest.PrefixGameTestTemplate(false)"
        else -> ""
    }

    fun reportName(id: String, module: TestModule): String = when (this) {
        FORGE_CONTAINER, NEOFORGE -> id
        FORGE_COMPONENTS -> "${module.id}.$id"
        FABRIC_VANILLA -> "${module.className.lowercase()}.$id"
        FABRIC_REGISTRY -> "${module.id}:${module.functionPrefix}_$id"
        FORGE_FUNCTIONS, NEOFORGE_FUNCTIONS -> "${module.id}:$id"
    }

    val functions get() = this in setOf(FORGE_FUNCTIONS, NEOFORGE_FUNCTIONS)

    val fabric get() = this in setOf(FABRIC_VANILLA, FABRIC_REGISTRY)

    val additionalReportNames: Set<String> get() = when (this) {
        FORGE_FUNCTIONS, NEOFORGE_FUNCTIONS, FABRIC_REGISTRY -> setOf("minecraft:always_pass")
        else -> emptySet()
    }

    fun entrypoint(cases: List<TestCase>, module: TestModule): String {
        require(cases.isNotEmpty() && cases.map { it.id }.distinct().size == cases.size) { "Test IDs must be nonempty and unique" }
        if (functions) return buildString {
            val neo = this@TestRegistration == NEOFORGE_FUNCTIONS
            appendLine("package ${module.packageName};")
            appendLine()
            appendLine("import net.minecraft.gametest.framework.GameTestHelper;")
            appendLine("import net.minecraft.core.registries.Registries;")
            appendLine("import ${if (neo) "net.neoforged.neoforge" else "net.minecraftforge"}.registries.DeferredRegister;")
            appendLine(if (neo) "import net.neoforged.bus.api.IEventBus;" else "import net.minecraftforge.eventbus.api.bus.BusGroup;")
            appendLine("import java.util.function.Consumer;")
            appendLine()
            appendLine("public final class ${module.className} {")
            appendLine("    private static final DeferredRegister<Consumer<GameTestHelper>> FUNCTIONS =")
            appendLine("            DeferredRegister.create(Registries.TEST_FUNCTION, \"${module.id}\");")
            appendLine("    static {")
            cases.forEach { case ->
                appendLine("        FUNCTIONS.register(\"${case.id}\", () -> ${case.owner}::run);")
            }
            appendLine("    }")
            appendLine("    public static void register(${if (neo) "IEventBus" else "BusGroup"} bus) { FUNCTIONS.register(bus); }")
            appendLine("    private ${module.className}() {}")
            appendLine("}")
        }
        return buildString {
            appendLine("package ${module.packageName};")
            appendLine()
            if (this@TestRegistration != FABRIC_REGISTRY) appendLine("import net.minecraft.gametest.framework.GameTest;")
            appendLine("import net.minecraft.gametest.framework.GameTestHelper;")
            appendLine()
            if (holder(module).isNotEmpty()) appendLine(holder(module))
            appendLine("public final class ${module.className} {")
            cases.forEach { case ->
                appendLine("    ${annotation(module)}")
                appendLine("    public ${if (fabric) "" else "static "}void ${case.id}(GameTestHelper helper) {")
                appendLine("        ${case.owner}.run(helper);")
                appendLine("    }")
                appendLine()
            }
            appendLine("}")
        }
    }
}

data class TestModule(val id: String, val packageName: String, val className: String, val reportProperty: String) {
    val functionPrefix get() = className.replace(Regex("([a-z])([A-Z])"), "$1_$2").lowercase()
    init {
        require(id.matches(Regex("[a-z][a-z0-9_]+")))
        require(packageName.split('.').all { it.matches(Regex("[a-z][a-z0-9]*")) })
        require(className.matches(Regex("[A-Z][A-Za-z0-9]*")))
    }
}
