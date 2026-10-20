plugins { `kotlin-dsl` }
group = rootProject.group
version = rootProject.version
repositories {
    gradlePluginPortal()
    mavenCentral()
}
kotlin { jvmToolchain(21) }
dependencies {
    implementation(project(":"))
    implementation(libs.moddev)
}
