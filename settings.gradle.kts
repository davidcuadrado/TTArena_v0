plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
rootProject.name = "TTArena_v0"

include("ability")
include("auth")
include("character")
include("map")
include("matchmaking")
include("user")
