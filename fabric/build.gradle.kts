plugins { `kotlin-dsl` }
group = rootProject.group
version = rootProject.version
repositories {
    maven("https://maven.fabricmc.net/")
    gradlePluginPortal()
    mavenCentral()
}
kotlin { jvmToolchain(21) }
dependencies {
    implementation(project(":"))
    implementation(libs.loom)
}
