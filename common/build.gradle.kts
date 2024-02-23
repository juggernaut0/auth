import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("dev.twarner.common")
    `maven-publish`
}

kotlin {
    jvm()
    js(IR) {
        browser()
    }
}

dependencies {
    commonMainApi(libs.multiplatformUtils)
}

tasks.withType<Kotlin2JsCompile> {
    kotlinOptions {
        moduleKind = "commonjs"
        sourceMap = true
        sourceMapEmbedSources = "always"
    }
}
