plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    compileOnly(gradleApi())
    implementation(kotlin("stdlib"))
    implementation("org.ow2.asm:asm-util:9.3")
}