plugins { `kotlin-dsl` }
group = rootProject.group
version = rootProject.version
repositories {
    gradlePluginPortal()
    mavenCentral()
}
kotlin { jvmToolchain(25) }
dependencies {
    implementation(project(":"))
    implementation(libs.moddev)
}
