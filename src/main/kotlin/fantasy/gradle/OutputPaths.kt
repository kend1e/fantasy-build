package fantasy.gradle

import java.nio.file.Path

internal fun outputPaths(root: Path, names: Set<String>): Map<String, Path> = names.associateWith { name ->
    val relative = Path.of(name)
    val destination = root.resolve(relative).normalize()
    require(name.isNotBlank() && !relative.isAbsolute && destination != root && destination.startsWith(root)) {
        "File path escapes its output directory or names the directory itself: $name"
    }
    destination
}
