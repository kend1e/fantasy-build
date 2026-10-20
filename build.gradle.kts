plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "net.kenddie.fantasy"
version = "0.1.0-SNAPSHOT"

repositories { mavenCentral() }

kotlin { jvmToolchain(21) }

dependencies {
    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
    maxParallelForks = 1
}

gradlePlugin {
    plugins {
        register("platform") {
            id = "fantasy.build.platform"
            implementationClass = "fantasy.gradle.PlatformPlugin"
            displayName = "Fantasy platform conventions"
            description = "Java compilation, shared sources and reproducible archives for a mod target."
        }
        register("resources") {
            id = "fantasy.build.resources"
            implementationClass = "fantasy.gradle.ResourcesPlugin"
            displayName = "Fantasy resource generation"
            description = "Declarative resource generation independent of Minecraft content and loaders."
        }
        register("shared") {
            id = "fantasy.build.shared"
            implementationClass = "fantasy.gradle.SharedPlugin"
            displayName = "Fantasy shared sources"
            description = "Exports common Java sources and processed resources to platform projects."
        }
    }
}
