// Root project: a container for the service modules, nothing more.
//
// It has no sources, so it deliberately does not apply the java or
// Spring Boot plugins - applying them made Gradle try to build a bootJar
// here and fail on a missing main class. Each module in settings.gradle.kts
// carries its own Java toolchain, Spring Boot version and dependencies.
//
// The `base` plugin only supplies the aggregate clean / build / check tasks
// that delegate to the modules.

plugins {
    base
}

group = "org.ttarena"
version = "0.0.1-SNAPSHOT"
