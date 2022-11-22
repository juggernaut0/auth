plugins {
    kotlin("jvm")
    id("dev.twarner.common")
    application
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation(libs.postgresql)
    implementation(libs.flyway.core)
}

application {
    mainClass.set("auth.MigrateKt")
}

tasks {
    run.invoke {
        args = listOf("postgres://auth:auth@localhost:6432/auth")
    }
}
