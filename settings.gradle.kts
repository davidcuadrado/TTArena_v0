plugins {
    // Lets Gradle download a matching JDK when the Java 25 toolchain
    // is not installed locally.
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "TTArena_v0"

include("map")
include("character")
include("user")
include("auth")
include("matchmaking")

