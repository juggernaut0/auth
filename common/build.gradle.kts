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
