import java.net.URI

plugins {

    kotlin("jvm") version "1.9.22"

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
    implementation("com.arbr:arbr-platform-alignable:1.0")

    // Logback + SLF4J
    compileOnly("ch.qos.logback:logback-classic:1.4.14")
    compileOnly("ch.qos.logback:logback-core:1.4.14")
    compileOnly("org.slf4j:slf4j-api:2.0.9")

    implementation("io.projectreactor:reactor-core:3.6.4")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0-rc1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0-rc1")

    // Apache commons text
    implementation("org.apache.commons:commons-text:1.10.0")

    // Apache Commons IO
    implementation("commons-io:commons-io:2.16.1")

    // Tokenization
    implementation("com.knuddels:jtokkit:0.6.1")

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("io.projectreactor:reactor-test:3.6.4")
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
            artifactId = "arbr-platform-object-graph"
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

kotlin {
    jvmToolchain(21)
}
