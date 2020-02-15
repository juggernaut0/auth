rootProject.name = "auth"
include("api-test")
include("common")
include("dbmigrate")
include("service")
include("ui")

project(":common").name = "auth-common"
project(":ui").name = "auth-ui"
