import java.net.URI
import kotlin.io.path.relativeTo

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
    compileOnly("com.arbr:arbr-codegen:1.0")

    // Logback + SLF4J
    compileOnly("ch.qos.logback:logback-classic:1.4.14")
    compileOnly("ch.qos.logback:logback-core:1.4.14")
    compileOnly("org.slf4j:slf4j-api:2.0.9")

    // Jackson
    compileOnly("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0-rc1")
    compileOnly("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0-rc1")

    // Mustache
    compileOnly("com.github.spullara.mustache.java:compiler:0.9.13")

    compileOnly("com.graphql-java:graphql-java:19.2")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

// Add templates to main sourceSet
kotlin.sourceSets["main"].kotlin.srcDir(
    "src/main/resources/templates",
)

kotlin {
    jvmToolchain(21)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

val templateManifestFile: Provider<RegularFile> = layout.buildDirectory.file("template-manifest.txt")
val listTemplates by tasks.creating(DefaultTask::class.java) {
    dependsOn(tasks.classes)
    val templateDir = layout.projectDirectory.dir("src/main/resources/templates")
    val filtered = templateDir.asFileTree.matching {
        include(
            "**/*.kttmpl",
            "**/*.kt.mustache",
        )
    }
    inputs.files(filtered)

    doFirst {
        val paths = filtered.files
            .map {
                it.toPath().relativeTo(layout.projectDirectory.asFile.toPath()).toString()
            }
            .sorted()
            .joinToString("\n")

        templateManifestFile.get().asFile.writeText(paths)
    }
    outputs.file(templateManifestFile)
}
tasks.withType<Jar> {
    dependsOn(listTemplates)
    from(templateManifestFile)
}

publishing {
    repositories {
        configuredMavenLocal()
    }
    publications {
        register<MavenPublication>("gpr") {
            artifactId = "arbr-codegen-target-object-model"
            from(components["kotlin"])
        }
    }
}

tasks.withType(AbstractPublishToMaven::class.java) {
    doFirst {
        println("Publishing version $version of ${project.name}...")
    }
    doLast {
        println("Published version $version of ${project.name}")
        publication.artifacts.forEach { artifact ->
            println("  - Artifact [classifier: ${artifact.classifier}]: ${artifact.file}")
        }
    }
}
