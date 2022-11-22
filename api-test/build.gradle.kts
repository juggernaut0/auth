plugins {
    kotlin("jvm")
    id("dev.twarner.common")
}

dependencies {
    testImplementation(kotlin("test-junit"))
    testImplementation(project(":auth-common"))
    testImplementation(platform(libs.ktor.bom))
    testImplementation(libs.ktor.client.apache)
}

tasks {
    test {
        enabled = false
    }

    val apiTest by registering(Test::class) {
        dependsOn("testClasses")
        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath
        outputs.upToDateWhen { false }
    }
}
