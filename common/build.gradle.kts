import com.moowork.gradle.node.task.NodeTask
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization").version("1.3.61")
    id("com.moowork.node")
    `maven-publish`
}

kotlin {
    jvm()
    js()

    sourceSets {
        val multiplatformUtilsVersion = "0.2.0-SNAPSHOT"

        val commonMain by getting {
            dependencies {
                api("com.github.juggernaut0:multiplatform-utils:$multiplatformUtilsVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:0.14.0")
            }
        }

        val jvmMain by getting {
            dependencies {
                api("com.github.juggernaut0:multiplatform-utils-jvm:$multiplatformUtilsVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")
            }
        }

        val jsMain by getting {
            dependencies {
                api("com.github.juggernaut0:multiplatform-utils-js:$multiplatformUtilsVersion")
                api("org.jetbrains.kotlinx:kotlinx-serialization-runtime-js:0.14.0")
            }
        }
    }
}

tasks.withType<Kotlin2JsCompile>().forEach {
    it.kotlinOptions.moduleKind = "umd"
}

/*tasks {
    val populateNodeModules by registering(Copy::class) {
        dependsOn("compileKotlinJs")

        from(getByName<Kotlin2JsCompile>("compileKotlinJs").destinationDir)

        configurations["jsTestCompileClasspath"].forEach {
            from(zipTree(it.absolutePath).matching { include("*.js") })
        }

        into("$buildDir/node_modules")
    }

    val runJest by registering(NodeTask::class) {
        dependsOn("jsTestClasses")
        dependsOn(populateNodeModules)
        setScript(file("node_modules/jest/bin/jest.js"))
    }

    getByName("jsTest").dependsOn(runJest)
    getByName("npmInstall").enabled = false
}*/
