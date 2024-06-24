import java.net.URI

plugins {
    kotlin("jvm") version "1.9.22"

    application
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
    // Logback + SLF4J
    implementation("ch.qos.logback:logback-classic:1.4.14")
    implementation("ch.qos.logback:logback-core:1.4.14")
    implementation("org.slf4j:slf4j-api:2.0.9")

    // Jackson
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0-rc1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0-rc1")

    // Commons IO
    implementation("commons-io:commons-io:2.15.1")

    implementation("com.graphql-java:graphql-java:19.2")
    implementation("org.openapitools:openapi-generator-cli:7.6.0")
    implementation("com.github.spullara.mustache.java:compiler:0.9.13")
    implementation("net.sourceforge.argparse4j:argparse4j:0.9.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

kotlin.sourceSets["main"].kotlin.srcDir(
    "src/main/resources/logback"
)

val mainClassName = "com.arbr.backdraft.TaskMainKt"
application {
    mainClass.set(mainClassName)
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
            artifactId = "arbr-codegen"
            from(components["kotlin"])
        }
    }
}

tasks.withType<Jar> {
    manifest {
        attributes("Main-Class" to mainClassName)
    }

    from(
        "src/main/resources/logback/"
    )
    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().filter { it.name.endsWith("jar") }.map { zipTree(it) }
    })
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    archiveBaseName = "codegen"
    archiveVersion = ""
}