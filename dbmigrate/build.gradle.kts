plugins {
    kotlin("jvm")
    application
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    implementation("org.postgresql:postgresql:42.2.5")
    implementation("org.flywaydb:flyway-core:5.2.4")
}

application {
    mainClassName = "MigrateKt"
}

tasks.getByName<JavaExec>("run") {
    args = listOf("postgres://auth:auth@localhost:6432/auth")
}
