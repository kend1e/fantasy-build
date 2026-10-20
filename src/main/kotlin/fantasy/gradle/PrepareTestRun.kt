package fantasy.gradle

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Minecraft rewrites configuration files during each run")
abstract class PrepareTestRun : DefaultTask() {
    @get:Input abstract val contents: MapProperty<String, String>
    @get:Input abstract val filesToRemove: SetProperty<String>
    @get:Internal abstract val runDirectory: DirectoryProperty

    init { outputs.upToDateWhen { false } }

    @TaskAction fun prepare() {
        val root = runDirectory.get().asFile.toPath()
        val paths = outputPaths(root, filesToRemove.get() + contents.get().keys)
        for (relative in filesToRemove.get()) {
            java.nio.file.Files.deleteIfExists(paths.getValue(relative))
        }
        for ((relative, contents) in contents.get()) {
            val destination = paths.getValue(relative)
            destination.toFile().apply { parentFile.mkdirs(); writeText(contents, Charsets.UTF_8) }
        }
    }
}
