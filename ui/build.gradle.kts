import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
    id("dev.twarner.common")
    `maven-publish`
}

kotlin {
    js(IR) {
        browser()
    }
}

dependencies {
    api(project(":auth-common"))

    api(libs.kui)
    implementation(libs.asyncLite)
}

tasks.withType<Kotlin2JsCompile>().configureEach {
    kotlinOptions.moduleKind = "umd"
}

publishing {
    publications {
        register("maven", MavenPublication::class) {
            from(components["kotlin"])
        }
    }
}
