plugins {
    kotlin("multiplatform") version "1.5.10" apply false
    kotlin("plugin.serialization") version "1.5.10" apply false
}

subprojects {
    group = "dev.twarner.auth"
    version = "3"

    repositories {
        mavenCentral()
        maven("https://juggernaut0.github.io/m2/repository")
        mavenLocal()
    }
}