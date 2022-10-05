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
    implementation("com.google.code.gson:gson:2.2.4")
    implementation("net.md-5:SpecialSource:1.11.0")
}