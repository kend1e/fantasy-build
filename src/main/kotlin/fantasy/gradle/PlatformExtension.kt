package fantasy.gradle

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.provider.Property

abstract class PlatformExtension {
    abstract val javaVersion: Property<Int>
    abstract val archiveName: Property<String>
    abstract val sources: ConfigurableFileCollection
}
