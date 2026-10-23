plugins { `kotlin-dsl` }
group = rootProject.group
version = rootProject.version
repositories {
    maven("https://maven.fabricmc.net/")
    gradlePluginPortal()
    mavenCentral()
}
kotlin { jvmToolchain(25) }
dependencies {
    implementation(project(":"))
    implementation(libs.loom)
}

gradlePlugin {
    plugins.register("fabric") {
        id = "fantasy.build.fabric"
        implementationClass = "fantasy.gradle.fabric.FabricPlugin"
    }
}
