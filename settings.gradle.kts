pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://juggernaut0.github.io/m2/repository")
    }

    plugins {
        val twarnerVersion = "0.3.4"
        id("dev.twarner.common") version twarnerVersion
        id("dev.twarner.docker") version twarnerVersion

        val kotlinVersion = "1.8.20"
        kotlin("js") version kotlinVersion
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("multiplatform") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }
}

dependencyResolutionManagement {
    repositories {
        maven("https://juggernaut0.github.io/m2/repository")
    }

    versionCatalogs {
        create("libs") {
            from("dev.twarner:catalog:0.3.4")
        }
    }
}

rootProject.name = "auth"
include("api-test")
include("common")
include("dbmigrate")
include("service")
include("ui")

project(":common").name = "auth-common"
project(":ui").name = "auth-ui"
