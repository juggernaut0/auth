import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    java
    application
    id("nu.studer.jooq") version "4.1"
    kotlin("kapt")
    id("com.bmuschko.docker-remote-api") version "6.7.0"
}

dependencies {
    implementation(project(":auth-common"))

    implementation(kotlin("stdlib-jdk8"))

    val ktorVersion = "1.6.0"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-jetty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    val daggerVersion = "2.36"
    implementation("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")

    implementation("io.github.config4k:config4k:0.4.2")

    implementation("ch.qos.logback:logback-classic:1.2.3")

    implementation("org.postgresql:postgresql:42.2.5")
    implementation("org.jooq:jooq:3.12.3")
    jooqRuntime("org.postgresql:postgresql:42.2.5")
    implementation("com.zaxxer:HikariCP:3.2.0")

    implementation("at.favre.lib:bcrypt:0.9.0")

    testImplementation(kotlin("test-junit"))
}

application {
    mainClassName = "auth.MainKt"
}

apply {
    from("jooq.gradle")
}
tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        dependsOn("generatePostgresJooqSchemaSource")
    }

    val copyDist by registering(Copy::class) {
        dependsOn(distTar)
        from(distTar.flatMap { it.archiveFile })
        into("$buildDir/docker")
    }

    val dockerfile by registering(Dockerfile::class) {
        dependsOn(copyDist)

        from("openjdk:11-jre-slim")
        addFile(distTar.flatMap { it.archiveFileName }.map { Dockerfile.File(it, "/app/") })
        defaultCommand(distTar.flatMap { it.archiveFile }.map { it.asFile.nameWithoutExtension }.map { listOf("/app/$it/bin/${project.name}") })
    }

    val dockerBuild by registering(DockerBuildImage::class) {
        dependsOn(dockerfile)

        if (version.toString().endsWith("SNAPSHOT")) {
            images.add("auth:SNAPSHOT")
        } else {
            images.add("juggernaut0/auth:$version")
        }
    }

    (run) {
        systemProperty("config.file", "local.conf")
    }
}
