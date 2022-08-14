plugins {
    `kotlin-dsl`
    kotlin("jvm") version "1.7.10" // TODO remove when gradle updates built in kotlin
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
    implementation("org.jetbrains.kotlin:kotlin-serialization:1.7.10")
}
