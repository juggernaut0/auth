import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile

plugins {
    kotlin("jvm")
    application
    id("com.bmuschko.docker-remote-api") version "6.7.0"
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.postgresql:postgresql:42.2.5")
    implementation("org.flywaydb:flyway-core:7.14.0")
}

application {
    mainClass.set("auth.MigrateKt")
}

tasks {
    run.invoke {
        args = listOf("postgres://auth:auth@localhost:6432/auth")
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
            images.add("auth-dbmigrate:SNAPSHOT")
        } else {
            images.add("juggernaut0/auth-dbmigrate:$version")
        }
    }
}
