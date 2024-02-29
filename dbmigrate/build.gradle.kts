plugins {
    id("dev.twarner.db")
}

tasks.startTestDb {
    databaseName = "auth"
}
