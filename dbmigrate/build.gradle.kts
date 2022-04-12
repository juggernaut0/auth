plugins {
    kotlin("jvm")
    id("common-conventions")
    application
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
}
