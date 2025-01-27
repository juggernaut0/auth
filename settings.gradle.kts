pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://juggernaut0.github.io/m2/repository")
    }

    plugins {
        val kotlinVersion = "2.1.10"
        kotlin("js") version kotlinVersion
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("multiplatform") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }
}

plugins {
    id("dev.twarner.settings") version "1.0.2"
}

rootProject.name = "auth"
include("api-test")
include("common")
include("dbmigrate")
include("plugins:ktor")
include("plugins:javalin")
include("service")
include("ui")

project(":common").name = "auth-common"
project(":plugins:ktor").name = "auth-plugins-ktor"
project(":plugins:javalin").name = "auth-plugins-javalin"
project(":ui").name = "auth-ui"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
