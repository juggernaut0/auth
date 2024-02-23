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
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.11")
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
        kotlinOptions.languageVersion = "1.6"
        compilerOptions.languageVersion
    }
}
