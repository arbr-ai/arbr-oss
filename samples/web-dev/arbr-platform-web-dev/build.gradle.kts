import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.plugin.ResolveMainClassName
import java.net.URI

plugins {
    id("org.springframework.boot") version "3.2.4"
    id("io.spring.dependency-management") version "1.1.4"

    kotlin("plugin.spring") version "1.9.22"
    kotlin("jvm") version "1.9.22"
    id("com.arbr.gradle") version "1.0"

    `java-library`
    `maven-publish`
}

group = "com.arbr"
version = "1.0"

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

dependencies {
    arbr("com.arbr:arbr-codegen:1.0")
    arbr("com.arbr:arbr-codegen-target-object-model:1.0")

    implementation("com.arbr:arbr-platform-base:1.0")
    implementation("com.arbr:arbr-platform:1.0")

    implementation("org.springframework:spring-beans:6.1.5")
    implementation("org.springframework:spring-context:6.1.5")
    implementation("org.springframework.boot:spring-boot-starter-webflux:3.2.4")

    implementation("io.projectreactor:reactor-core:3.6.4")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")

    // Logback + SLF4J
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("ch.qos.logback:logback-core:1.4.14")
    implementation("org.slf4j:slf4j-api:2.0.9")

    // Jackson
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0-rc1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0-rc1")

    // Commons IO + Text
    implementation("commons-io:commons-io:2.15.1")
    implementation("org.apache.commons:commons-text:1.10.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.withType<ResolveMainClassName> {
    configuredMainClassName.set("com.arbr.core_web_dev.PlatformWebDevMainKt")
}

kotlin {
    jvmToolchain(21)
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
            artifactId = "arbr-platform-web-dev"
            from(components["kotlin"])
            artifact(tasks.kotlinSourcesJar)
        }
    }
}

/**
 * CORE OBJECT MODEL SCHEMA
 */
arbr {
    domain = "arbr"

    val generateSourceFromGraphQLSchema by compileSchema.creating {
        this.filter { spec ->
            spec.include("**/*.graphqls", "**/*.graphql")
        }
        target.set("com.arbr.codegen.target.compiler.om.GraphQlSchemaToObjectModelCompiler")
    }
    tasks.kotlinSourcesJar {
        dependsOn(generateSourceFromGraphQLSchema)
    }
    tasks.withType<KotlinCompile> {
        dependsOn(generateSourceFromGraphQLSchema)
    }
    tasks.test {
        dependsOn(generateSourceFromGraphQLSchema)
        useJUnitPlatform()
    }
    tasks.withType<Jar> {
        dependsOn(generateSourceFromGraphQLSchema)
        archiveBaseName = "platform-web-dev"
        archiveVersion = ""

        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

