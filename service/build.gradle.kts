import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    java
    application
    id("nu.studer.jooq") version "4.1"
    kotlin("kapt")
    id("com.bmuschko.docker-remote-api") version "6.1.1"
}

dependencies {
    implementation(project(":auth-common"))

    implementation(kotlin("stdlib-jdk8"))

    val ktorVersion = "1.2.6"
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-jetty:$ktorVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-html-builder:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")

    val daggerVersion = "2.25.4"
    implementation("com.google.dagger:dagger:$daggerVersion")
    kapt("com.google.dagger:dagger-compiler:$daggerVersion")

    implementation("io.github.config4k:config4k:0.4.1")

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
    withType<KotlinCompile>().forEach {
        it.kotlinOptions.jvmTarget = "1.8"
        it.dependsOn("generatePostgresJooqSchemaSource")
    }

    val distTar = getByName<Tar>("distTar")

    val copyDist by registering(Copy::class) {
        dependsOn(distTar)
        from(distTar.archiveFile)
        into("$buildDir/docker")
    }

    val dockerfile by registering(com.bmuschko.gradle.docker.tasks.image.Dockerfile::class) {
        dependsOn(copyDist)

        from("openjdk:8-alpine")
        addFile("${project.name}-$version.tar", "/app/")
        defaultCommand("/app/${project.name}-$version/bin/${project.name}")
    }

    val dockerBuild by registering(com.bmuschko.gradle.docker.tasks.image.DockerBuildImage::class) {
        dependsOn(dockerfile)

        images.add("twarner.dev/auth:$version")
    }

    val run by getting(JavaExec::class) {
        systemProperty("config.file", "local.conf")
    }
}
