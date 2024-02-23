import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("dev.twarner.common")
    `maven-publish`
}

dependencies {
    api(projects.authCommon)
    api("com.github.juggernaut0:multiplatform-utils-javalin:${libs.versions.multiplatform.utils.get()}")

    testImplementation(kotlin("test"))
    testRuntimeOnly("org.slf4j:slf4j-simple:2.0.11")
    testImplementation("io.mockk:mockk:1.13.9")
    testImplementation("com.github.juggernaut0:multiplatform-utils-javalin:${libs.versions.multiplatform.utils.get()}")
    testImplementation("io.javalin:javalin-testtools:6.1.0")
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
