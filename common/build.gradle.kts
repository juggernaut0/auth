import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("dev.twarner.common")
    `maven-publish`
}

kotlin {
    jvm()
    js {
        browser()
    }
}

dependencies {
    commonMainApi(libs.multiplatform.utils)

    "jvmMainApi"(libs.multiplatform.utils.ktor)

    "jvmTestImplementation"(kotlin("test-junit"))
    "jvmTestImplementation"(platform(libs.ktor.bom))
    "jvmTestImplementation"(libs.ktor.server.testHost)
    "jvmTestImplementation"(libs.ktor.client.mock)
}

tasks.withType<Kotlin2JsCompile> {
    kotlinOptions {
        moduleKind = "commonjs"
        sourceMap = true
        sourceMapEmbedSources = "always"
    }
}
