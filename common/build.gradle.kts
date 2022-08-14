import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("common-conventions")
    `maven-publish`
}

kotlin {
    jvm()
    js {
        browser()
    }

    sourceSets {
        val multiplatformUtilsVersion = "0.7.0"

        commonMain {
            dependencies {
                api("com.github.juggernaut0:multiplatform-utils:$multiplatformUtilsVersion")
            }
        }

        named("jvmMain") {
            dependencies {
                api("com.github.juggernaut0:multiplatform-utils-ktor-jvm:$multiplatformUtilsVersion")
            }
        }

        named("jvmTest") {
            dependencies {
                implementation(kotlin("test-junit"))
                val ktorVersion = "2.1.0"
                implementation("io.ktor:ktor-server-test-host:$ktorVersion")
                implementation("io.ktor:ktor-client-mock:$ktorVersion")

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
