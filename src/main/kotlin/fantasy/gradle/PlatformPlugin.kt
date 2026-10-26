package fantasy.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.BasePluginExtension
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.AbstractArchiveTask
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.*

class PlatformPlugin : Plugin<Project> {
    override fun apply(project: Project): Unit = with(project) {
        pluginManager.apply("java")
        val platform = extensions.create<PlatformExtension>("fantasyPlatform")
        platform.archiveName.convention(name)
        extensions.configure<JavaPluginExtension> {
            toolchain.languageVersion.set(platform.javaVersion.map(JavaLanguageVersion::of))
        }
        extensions.configure<BasePluginExtension> { archivesName.convention(platform.archiveName) }
        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(platform.javaVersion)
        }
        tasks.withType<AbstractArchiveTask>().configureEach {
            isPreserveFileTimestamps = false
            isReproducibleFileOrder = true
        }
        tasks.named<Jar>("jar") {
            from(rootProject.layout.projectDirectory.file("LICENSE.md"))
        }
        val sharedSourcesDependencies = configurations.dependencyScope("sharedSourcesDependencies")
        val sharedSources = configurations.resolvable("sharedSourceFiles") {
            extendsFrom(sharedSourcesDependencies.get())
            isTransitive = false
        }
        val sharedResourcesDependencies = configurations.dependencyScope("sharedResourcesDependencies")
        val sharedResources = configurations.resolvable("sharedResourceFiles") {
            extendsFrom(sharedResourcesDependencies.get())
            isTransitive = false
        }
        extensions.getByType<SourceSetContainer>().named("main") {
            java.srcDir(platform.sources)
            java.srcDir(sharedSources)
            resources.srcDir(sharedResources)
        }
        tasks.named<org.gradle.language.jvm.tasks.ProcessResources>("processResources") {
            duplicatesStrategy = DuplicatesStrategy.FAIL
        }
    }
}
