plugins { `kotlin-dsl` }
group = rootProject.group
version = rootProject.version
repositories {
    gradlePluginPortal()
    maven("https://maven.minecraftforge.net/")
    mavenCentral()
}
kotlin { jvmToolchain(21) }
dependencies {
    implementation(project(":"))
    implementation(libs.forgeGradle)
    implementation(libs.renamer)
}
