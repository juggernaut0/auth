import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("dev.twarner.common")
    `maven-publish`
}

dependencies {
    api(libs.multiplatformUtils.ktor)
    api(projects.authCommon)

    testImplementation(kotlin("test"))
    testRuntimeOnly(libs.slf4j.simple)
    testImplementation(platform(libs.ktor.bom))
    testImplementation(libs.ktor.server.testHost)
    testImplementation(libs.ktor.client.mock)
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
