import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    java
    id("org.springframework.boot") version "4.1.1"
    jacoco
}

group = "org.ttarena"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot's BOM, applied through Gradle's own platform support instead of
    // the io.spring.dependency-management plugin. It has to be added to every
    // configuration that is not derived from `implementation`, otherwise the
    // version-less dependencies below cannot be resolved.
    val springBootBom = platform(SpringBootPlugin.BOM_COORDINATES)
    implementation(springBootBom)
    compileOnly(springBootBom)
    annotationProcessor(springBootBom)
    developmentOnly(springBootBom)
    testImplementation(springBootBom)
    testRuntimeOnly(springBootBom)

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    // OpenAPI / Swagger UI. springdoc 3.x is the Spring Boot 4 line; the WebFlux
    // starter is the right one for a reactive application.
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:3.1.0")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.security:spring-security-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
