package fantasy.gradle

import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

abstract class ResourcesExtension @Inject constructor(objects: ObjectFactory) {
    abstract val sources: ConfigurableFileCollection
    val generators: NamedDomainObjectContainer<ResourceGeneration> =
        objects.domainObjectContainer(ResourceGeneration::class.java) { name ->
            objects.newInstance(ResourceGeneration::class.java, name)
        }

    fun generate(taskName: String, configure: Action<ResourceGeneration>) {
        require(taskName.matches(Regex("[a-z][A-Za-z0-9]*"))) { "Invalid resource task name: $taskName" }
        generators.create(taskName, configure)
    }
}
