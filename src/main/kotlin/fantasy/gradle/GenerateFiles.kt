package fantasy.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileSystemOperations
import org.gradle.api.provider.MapProperty
import org.gradle.api.tasks.*
import javax.inject.Inject

@CacheableTask
abstract class GenerateFiles : DefaultTask() {
    @get:Input abstract val contents: MapProperty<String, String>
    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty
    @get:Inject abstract val fileSystem: FileSystemOperations

    @TaskAction fun generate() {
        val root = outputDirectory.get().asFile.toPath()
        val files = contents.get()
        val paths = outputPaths(root, files.keys)
        fileSystem.delete { delete(root) }
        for ((name, text) in files.toSortedMap()) {
            val file = paths.getValue(name).toFile()
            file.parentFile.mkdirs()
            file.writeText(text, Charsets.UTF_8)
        }
    }
}
