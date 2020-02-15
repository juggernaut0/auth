plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(kotlin("test-junit"))
    testImplementation(project(":auth-common"))
    testImplementation("io.ktor:ktor-client-apache:1.2.6")
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
