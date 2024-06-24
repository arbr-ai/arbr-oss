import java.net.URI
import kotlin.properties.ReadOnlyProperty

plugins {
    kotlin("jvm") version "1.9.22"

    `maven-publish`
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.0.0"
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

private val commonGroup by requiredProperty("arbr.plugin.group")
private val pluginArtifactId by requiredProperty("arbr.plugin.artifact-id")
private val pluginVersion by requiredProperty("arbr.plugin.version")
private val pluginId = "${commonGroup}.${pluginArtifactId}"

dependencies {
    implementation("com.arbr:arbr-codegen:1.0")

    // https://mvnrepository.com/artifact/org.jetbrains.kotlin/kotlin-gradle-plugin
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")

    // Logback + SLF4J
    compileOnly("ch.qos.logback:logback-classic:1.4.14")
    compileOnly("ch.qos.logback:logback-core:1.4.14")
    compileOnly("org.slf4j:slf4j-api:2.0.9")

    // Jackson
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0-rc1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0-rc1")

    // Commons IO
    implementation("commons-io:commons-io:2.15.1")

    implementation("com.graphql-java:graphql-java:19.2")
    implementation("org.openapitools:openapi-generator-cli:7.6.0")
    implementation("com.github.spullara.mustache.java:compiler:0.9.13")
    implementation("net.sourceforge.argparse4j:argparse4j:0.9.0")
}

kotlin {
    jvmToolchain(21)
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

gradlePlugin {
    plugins {
        create("customPlugin") {
            id = pluginId
            version = pluginVersion
            implementationClass = "com.arbr.gradle.ArbrPlugin"
        }
    }
}

tasks.withType<Jar>() {
    from(
        "src/main/resources/templates"
    )
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

publishing {
    group = commonGroup
    version = pluginVersion
    repositories {
        configuredMavenLocal()
    }
    publications.withType<MavenPublication>() {
        artifactId = pluginArtifactId
    }
}

tasks.withType(AbstractPublishToMaven::class.java) {
    val apm = this

    apm.doFirst {
        val publication = apm.publication
        println("Publishing version $version of ${project.name}...")
        println(publication::class.java.canonicalName)
        println("Publication: ${publication.name}: ${publication.groupId}:${publication.artifactId}:${publication.version}")
    }
    apm.doLast {
        val publication = apm.publication
        println("Published version $version of ${project.name}")
        println("Publication ${publication.name}: ${publication.groupId}:${publication.artifactId}:${publication.version}")
        publication.artifacts.forEach {
            println(it.file)
        }
    }
}

/**
 * Utils
 */

fun <T> requiredProperty(key: String) = ReadOnlyProperty<T, String> { _, _ ->
    val checkedProjectNames = mutableListOf<String>()
    var candidateProject: Project? = project
    var foundPropertyValue: String? = null
    while (candidateProject != null) {
        foundPropertyValue = candidateProject.properties[key]?.toString()
        checkedProjectNames.add(candidateProject.name)
        if (foundPropertyValue != null) {
            break
        }
        candidateProject = candidateProject.parent
    }

    if (foundPropertyValue == null) {
        throw IllegalArgumentException(
            "Unable to find property with key '$key' in gradle properties. Checked in the following projects:\n"
                    + checkedProjectNames.joinToString("\n") { "  - $it" }
        )
    }

    foundPropertyValue
}