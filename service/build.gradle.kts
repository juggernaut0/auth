import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("dev.twarner.common")
    java
    application
    alias(libs.plugins.jooq)
    alias(libs.plugins.docker.api)
}

dependencies {
    implementation(project(":auth-common"))
    implementation(project(":dbmigrate"))

    implementation(kotlin("stdlib-jdk8"))

    implementation(platform(libs.ktor.bom))
    implementation(libs.bundles.ktor.server.jetty)
    implementation(libs.ktor.server.auth)
    implementation("io.ktor:ktor-server-auth-jwt")
    implementation("io.ktor:ktor-server-html-builder")
    implementation(libs.ktor.client.cio)

    implementation(libs.dagger)
    kapt(libs.dagger.compiler)

    implementation(libs.config4k)

    implementation(libs.logback)

    jooqGenerator(libs.postgresql)
    implementation(libs.bundles.r2dbc.postgresql)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.6.4")

    implementation("at.favre.lib:bcrypt:0.9.0")

    implementation("com.google.api-client:google-api-client:2.0.1")

    testImplementation(kotlin("test-junit"))
}

application {
    mainClass.set("auth.MainKt")
}

jooq {
    configurations {
        create("main") {
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
    withType<KotlinCompile>().configureEach {
        kotlinOptions.jvmTarget = "17"
    }

    val copyDist by registering(Copy::class) {
        dependsOn(distTar)
        from(distTar.flatMap { it.archiveFile })
        into("$buildDir/docker")
    }

    val dockerfile by registering(Dockerfile::class) {
        dependsOn(copyDist)

        from("openjdk:17-slim")
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
