import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("dev.twarner.common")
    `maven-publish`
}

dependencies {
    api(projects.authCommon)
    api(libs.multiplatformUtils.javalin)

    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.slf4j.simple)
    testImplementation(libs.mockk)
    testImplementation(libs.javalin.testtools)
}

java {
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    withType<JavaCompile>().configureEach {
        options.release = 17
    }

    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "17"
    }
}
