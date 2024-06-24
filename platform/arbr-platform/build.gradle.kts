import java.net.URI

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.spring") version "1.9.22"

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
    implementation("com.arbr:arbr-platform-alignable:1.0")
    implementation("com.arbr:arbr-platform-object-graph:1.0")
//    implementation(project(":arbr-platform-base"))

    implementation("org.springframework.boot:spring-boot-starter-webflux:3.2.4")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0-rc1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0-rc1")

    // Circuit breakers
    implementation("io.github.resilience4j:resilience4j-ratelimiter:2.0.2")
    implementation("io.github.resilience4j:resilience4j-reactor:2.0.2")

    // Kafka streams
    implementation("org.apache.kafka:kafka-streams:3.6.1")
    implementation("org.springframework.kafka:spring-kafka:3.1.3")
    implementation("io.projectreactor.kafka:reactor-kafka:1.3.22")

    // AWS MSK (Kafka)
    implementation("software.amazon.msk:aws-msk-iam-auth:2.0.3")

    // AWS SDK + S3
    implementation("software.amazon.awssdk:s3:2.17.184")
    implementation("software.amazon.awssdk:core:2.17.184")

    // https://mvnrepository.com/artifact/org.antlr/antlr4
    implementation("org.antlr:antlr4:4.13.1")
    implementation("org.antlr:antlr4-runtime:4.13.1")

    // Matrix math
    implementation("org.ejml:ejml-all:0.43.1")

    // postgres + r2dbc
    implementation("org.postgresql:postgresql:42.5.4")
    implementation("org.postgresql:r2dbc-postgresql:1.0.4.RELEASE")
    implementation("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")

    // jooq
    implementation("org.jooq:jooq:${jooqVersion}")
    implementation("org.jooq:jooq-kotlin:${jooqVersion}")

    // Apache commons text
    implementation("org.apache.commons:commons-text:1.10.0")

    // Apache Commons IO
    implementation("commons-io:commons-io:2.16.1")

    // Tokenization
    implementation("com.knuddels:jtokkit:0.6.1")

    // Cryptography
    implementation("org.bouncycastle:bcprov-jdk18on:1.77")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:3.2.4")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.4")
    testImplementation("io.projectreactor:reactor-test:3.6.4")
//    testImplementation("org.springframework.kafka:spring-kafka-test:3.1.3")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-core:5.6.0")

}

java {
    withSourcesJar()
}
publishing {
    repositories {
        configuredMavenLocal()
    }
    publications {
        register<MavenPublication>("gpr") {
            artifactId = "arbr-platform"
            from(components["kotlin"])
            artifact(tasks.kotlinSourcesJar)
        }
    }
}

tasks.withType(AbstractPublishToMaven::class.java) {
    doFirst {
        println("Publishing version $version of ${project.name}...")
    }
    doLast {
        println("Published version $version of ${project.name}")
    }
}

buildscript {
    configurations["classpath"].resolutionStrategy.eachDependency {
        if (requested.group == "org.jooq") {
            useVersion("3.18.5")
        }
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    archiveBaseName = "platform"
    archiveVersion = ""
}

