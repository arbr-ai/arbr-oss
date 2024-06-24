import java.net.URI

plugins {
    kotlin("plugin.spring") version "1.9.22"
    kotlin("jvm") version "1.9.22"

    `java-library`
    `maven-publish`
}

group = "com.arbr"
version = "1.0"

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

fun RepositoryHandler.configuredMavenLocal() = mavenLocal {
    System.getenv("MAVEN_LOCAL_HOST")?.takeIf { it.isNotBlank() }?.let { localMavenHost ->
        isAllowInsecureProtocol = true
        url = URI("http://$localMavenHost/releases")
        credentials {
            username = "admin"
            password = "secret"
        }
    }
}

repositories {
    configuredMavenLocal()
    mavenCentral()
}

val jooqVersion = project.properties["jooqVersion"] ?: "3.18.5"

dependencies {
    implementation("com.arbr:arbr-platform-base:1.0")

    // Spring WebFlux + Reactor + Jackson
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.2.4")
    implementation("org.springframework.boot:spring-boot-starter-security:3.2.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0-rc1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0-rc1")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")

    // Logback + SLF4J
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("ch.qos.logback:logback-core:1.4.14")
    implementation("org.slf4j:slf4j-api:2.0.9")

    // Jackson
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0-rc1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0-rc1")

    // Commons IO + Text
    implementation("commons-io:commons-io:2.16.1")
    implementation("org.apache.commons:commons-text:1.10.0")

    // Postgres + R2DBC + jOOQ
    implementation("org.postgresql:postgresql:42.5.4")
    implementation("org.postgresql:r2dbc-postgresql:1.0.4.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    implementation("org.jooq:jooq:${jooqVersion}")
    implementation("org.jooq:jooq-kotlin:${jooqVersion}")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // Crypto + Resilience4j
    implementation("org.bouncycastle:bcprov-jdk18on:1.77")
    implementation("io.github.resilience4j:resilience4j-ratelimiter:2.0.2")
    implementation("io.github.resilience4j:resilience4j-reactor:2.0.2")

    // AWS SDK + S3
    implementation("software.amazon.awssdk:s3:2.17.184")
    implementation("software.amazon.awssdk:core:2.17.184")

    // Kafka + MSK
    implementation("org.apache.kafka:kafka-streams:3.6.1")
    implementation("org.springframework.kafka:spring-kafka:3.1.3")
    implementation("io.projectreactor.kafka:reactor-kafka:1.3.22")
    implementation("software.amazon.msk:aws-msk-iam-auth:2.0.3")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:3.2.4")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}

publishing {
    repositories {
        configuredMavenLocal()
    }
    publications {
        register<MavenPublication>("gpr") {
            artifactId = "arbr-api-base"
            from(components["kotlin"])
            artifact(tasks.kotlinSourcesJar)
        }
    }
}
