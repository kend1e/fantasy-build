package fantasy.gradle

import org.gradle.api.Named
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.MapProperty
import javax.inject.Inject

abstract class ResourceGeneration @Inject constructor(private val name: String) : Named {
    override fun getName(): String = name
    abstract val contents: MapProperty<String, String>
    abstract val outputDirectory: DirectoryProperty
}
