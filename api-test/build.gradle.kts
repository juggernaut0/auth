plugins {
    kotlin("jvm")
    id("common-conventions")
}

dependencies {
    testImplementation(kotlin("test-junit"))
    testImplementation(project(":auth-common"))
    testImplementation("io.ktor:ktor-client-apache:2.0.2")
}

tasks {
    test {
        enabled = false
    }

    val apiTest by registering(Test::class) {
        dependsOn("testClasses")
        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath
    }
}
