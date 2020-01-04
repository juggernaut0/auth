import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("js")
}

dependencies {
    compile(project(":common"))

    compile(kotlin("stdlib-js"))
    compile("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.1.1")

    compile("com.github.juggernaut0.kui:kui:0.7.0")
}

tasks.withType<Kotlin2JsCompile>().forEach {
    it.kotlinOptions.moduleKind = "umd"
}
