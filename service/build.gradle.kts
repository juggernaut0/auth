import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    java
    application
    id("nu.studer.jooq") version "5.2.1"
    kotlin("kapt")
    id("com.bmuschko.docker-remote-api") version "6.7.0"
}

dependencies {
    implementation(project(":auth-common"))
    implementation(project(":dbmigrate"))

    implementation(kotlin("stdlib-jdk8"))

    val ktorVersion = "1.6.2"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-jetty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    val daggerVersion = "2.38.1"
    implementation("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")

    implementation("io.github.config4k:config4k:0.4.2")

    implementation("ch.qos.logback:logback-classic:1.2.5")

    implementation("org.jooq:jooq:3.15.1")
    jooqGenerator("org.postgresql:postgresql:42.2.23")
    implementation("io.r2dbc:r2dbc-postgresql:0.8.8.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:0.8.7.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.5.1")

    implementation("at.favre.lib:bcrypt:0.9.0")

    testImplementation(kotlin("test-junit"))
}

application {
    mainClass.set("auth.MainKt")
}

jooq {
    configurations {
        create("main") {
            version.set("3.15.1")
            generateSchemaSourceOnCompilation.set(true)
            jooqConfiguration.apply {
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://localhost:6432/auth"
                    user = "auth"
                    password = "auth"
                }
                generator.apply {
                    name = "org.jooq.codegen.DefaultGenerator"
                    strategy.apply {
                        name = "org.jooq.codegen.DefaultGeneratorStrategy"
                    }
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                        includes = ".*"
                        excludes = "flyway_schema_history"
                    }
                    generate.apply {
                        isRelations = true
                        isDeprecated = false
                        isRecords = true
                        isFluentSetters = false
                    }
                    target.apply {
                        packageName = "auth.db.jooq"
                        directory = "build/generated/source/jooq/main"
                    }
                }
            }
        }
    }
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
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
