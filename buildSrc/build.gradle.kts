plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    compileOnly(gradleApi())
    implementation(kotlin("stdlib"))
    implementation("org.ow2.asm:asm-tree:9.3")
    implementation("com.beust:klaxon:5.5")
    implementation("net.md-5:SpecialSource:1.11.0")
}