plugins {
    kotlin("multiplatform") version "1.3.61" apply false
    id("com.moowork.node") version "1.2.0" apply false
}

subprojects {
    group = "dev.twarner.auth"
    version = "2"

    repositories {
        mavenCentral()
        maven("https://kotlin.bintray.com/kotlinx")
        jcenter()
        maven("https://juggernaut0.github.io/m2/repository")
        mavenLocal()
    }
}