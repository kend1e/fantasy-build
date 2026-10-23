package fantasy.gradle.resources

import com.google.gson.GsonBuilder

private val gson = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()
fun json(value: Any): String = gson.toJson(value) + "\n"
fun mergeResources(vararg groups: Map<String, String>): Map<String, String> = buildMap {
    for (group in groups) for ((path, value) in group)
        require(putIfAbsent(path, value) == null) { "Duplicate generated resource: $path" }
}
