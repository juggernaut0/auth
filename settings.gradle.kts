pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://juggernaut0.github.io/m2/repository")
    }

    plugins {
        val twarnerVersion = "0.3.6"
        id("dev.twarner.common") version twarnerVersion
        id("dev.twarner.docker") version twarnerVersion

        val kotlinVersion = "1.9.22"
        kotlin("js") version kotlinVersion
        kotlin("jvm") version kotlinVersion
        kotlin("kapt") version kotlinVersion
        kotlin("multiplatform") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }
}

dependencyResolutionManagement {
    repositories {
        mavenLocal()
        maven("https://juggernaut0.github.io/m2/repository")
    }

    versionCatalogs {
        create("libs") {
            from("dev.twarner:catalog:0.3.6")
            version("multiplatform.utils", "0.10.0")
            version("dagger", "2.50")
        }
    }
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
