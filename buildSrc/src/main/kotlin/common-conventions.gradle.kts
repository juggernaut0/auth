val projectVersion: String by rootProject

group = "dev.twarner.auth"
version = projectVersion


repositories {
    mavenLocal()
    mavenCentral()
    maven("https://juggernaut0.github.io/m2/repository")
    google()
}
