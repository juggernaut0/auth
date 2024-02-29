plugins {
    kotlin("jvm")
    kotlin("kapt")
    id("dev.twarner.common")
    id("dev.twarner.docker")
    application
}

dependencies {
    implementation(projects.authCommon)
    implementation(projects.dbmigrate)

    implementation(kotlin("stdlib-jdk8"))

    implementation(libs.multiplatformUtils.ktor)

    implementation(platform(libs.ktor.bom))
    implementation(libs.bundles.ktor.server.jetty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.client.cio)

    implementation(libs.dagger)
    kapt(libs.dagger.compiler)

    implementation(libs.config4k)

    implementation(libs.logback)

    implementation(libs.bundles.r2dbc.postgresql)
    implementation(libs.kotlinx.coroutines.reactor)

    implementation("at.favre.lib:bcrypt:0.10.2")

    implementation("com.google.api-client:google-api-client:2.3.0")
}

application {
    mainClass.set("auth.MainKt")
}

tasks {
    (run) {
        systemProperty("config.file", "local.conf")
    }
}
