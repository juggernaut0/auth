import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

kotlin {
    jvm()
    js {
        browser()
    }

    sourceSets {
        val multiplatformUtilsVersion = "0.6.3"

        val commonMain by getting {
            dependencies {
                api("com.github.juggernaut0:multiplatform-utils:$multiplatformUtilsVersion")
            }
        }

        val jvmMain by getting {
            dependencies {
                api("com.github.juggernaut0:multiplatform-utils-ktor-jvm:$multiplatformUtilsVersion")
            }
        }
    }
}

tasks.withType<Kotlin2JsCompile> {
    kotlinOptions {
        moduleKind = "commonjs"
        sourceMap = true
        sourceMapEmbedSources = "always"
    }
}
