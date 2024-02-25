plugins {
    kotlin("multiplatform")
    id("dev.twarner.common")
    `maven-publish`
}

kotlin {
    js(IR) {
        browser()
    }
}

dependencies {
    "jsMainApi"(projects.authCommon)

    "jsMainApi"(libs.kui)
    "jsMainImplementation"(libs.asyncLite)
}
