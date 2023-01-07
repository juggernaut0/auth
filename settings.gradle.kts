pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://juggernaut0.github.io/m2/repository")
    }

    plugins {
        id("dev.twarner.common") version "0.3.2"

        val kotlinVersion = "1.8.0"
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
            from("dev.twarner:catalog:0.3.2")
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
