package fantasy.gradle.testing

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Copies a tiny bundled test fixture")
abstract class CopyTestStructure : DefaultTask() {
    @get:OutputFile abstract val destination: RegularFileProperty

    @TaskAction
    fun copy() {
        val output = destination.get().asFile
        output.parentFile.mkdirs()
        checkNotNull(javaClass.getResourceAsStream("/fantasy/testing/empty.nbt")).use { source ->
            output.outputStream().use { source.copyTo(it) }
        }
    }
}
