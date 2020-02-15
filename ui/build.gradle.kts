import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
    `maven-publish`
}

dependencies {
    api(project(":auth-common"))

    implementation(kotlin("stdlib-js"))

    api("com.github.juggernaut0.kui:kui:0.10.0")
    implementation("com.github.juggernaut0:async-lite:0.1.0")
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
