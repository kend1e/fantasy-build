plugins {
    `kotlin-dsl`
    `maven-publish`
}

group = "net.kenddie.fantasy"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
    maven("https://maven.fabricmc.net/")
    maven("https://maven.minecraftforge.net/")
}

kotlin { jvmToolchain(21) }

dependencies {
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation(gradleTestKit())
    testImplementation("junit:junit:4.13.2")
    testImplementation(project(":fabric"))
    testImplementation(project(":forge"))
    testImplementation(project(":neoforge"))
}

tasks.test {
    useJUnit()
    maxParallelForks = 1
    systemProperty("adapterTestClasspath", sourceSets.test.get().runtimeClasspath.asPath)
    systemProperty("adapterGradleUserHome", gradle.gradleUserHomeDir.absolutePath)
}

tasks.named("check") {
    dependsOn(subprojects.map { "${it.path}:validatePlugins" })
}

gradlePlugin {
    plugins {
        register("lifecycle") {
            id = "fantasy.build.lifecycle"
            implementationClass = "fantasy.gradle.LifecyclePlugin"
        }
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
