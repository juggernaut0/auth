import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
    id("dev.twarner.common")
    `maven-publish`
}

kotlin {
    js {
        browser()
    }
}

dependencies {
    api(project(":auth-common"))

    api(libs.kui)
    implementation("com.github.juggernaut0:async-lite:0.2.0")
}

tasks.withType<Kotlin2JsCompile>().forEach {
    it.kotlinOptions.moduleKind = "umd"
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from("$projectDir/src/main/kotlin")
}

publishing {
    publications {
        register("maven", MavenPublication::class) {
            from(components["kotlin"])
            artifact(sourcesJar.get())
        }
    }
}
