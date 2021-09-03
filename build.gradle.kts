plugins {
    kotlin("multiplatform") version "1.5.30" apply false
    kotlin("plugin.serialization") version "1.5.30" apply false
}

subprojects {
    group = "dev.twarner.auth"
    version = "5"

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://juggernaut0.github.io/m2/repository")
        google()
    }
}
