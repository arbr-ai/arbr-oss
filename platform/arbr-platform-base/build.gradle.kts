import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.SchemaMappingType
import java.net.URI
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

plugins {
    id("nu.studer.jooq") version "8.2"

    kotlin("jvm") version "1.9.22"

    antlr
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

val jooqVersion = project.properties["jooqVersion"] ?: "3.18.5"

dependencies {
    antlr("org.antlr:antlr4:4.13.1")

    // Logback + SLF4J
    compileOnly("ch.qos.logback:logback-classic:1.4.14")
    compileOnly("ch.qos.logback:logback-core:1.4.14")
    compileOnly("org.slf4j:slf4j-api:2.0.9")

    implementation("io.projectreactor:reactor-core:3.6.4")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.2.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.22")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.0-rc1")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.0-rc1")

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

    // https://mvnrepository.com/artifact/com.google.code.gson/gson
    implementation("com.google.code.gson:gson:2.10.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.8.1")
    testImplementation("io.projectreactor:reactor-test:3.6.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.22")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.9.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.1.0")
    testImplementation("org.mockito:mockito-core:5.6.0")

    /**
     * jOOQ codegen dependencies
     */
    jooqGenerator("org.postgresql:postgresql:42.5.4")
    jooqGenerator("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
    jooqGenerator("org.jooq:jooq-meta-extensions:${jooqVersion}")
    // workaround of array type codegen, see: https://github.com/jOOQ/jOOQ/issues/13322
    jooqGenerator("com.h2database:h2:2.1.214")

}

//arbr {
//    compileSchema.create("langParsers") {
//        target = "antlr"
//        this.filter { pattern ->
//            pattern.include("**/*.g4")
//        }
////        outputBaseDir.dir(
////            projet.layout.buildDirectory
////        )
//    }
//}

java {
    withSourcesJar()
}
publishing {
    repositories {
        configuredMavenLocal()
    }
    publications {
        register<MavenPublication>("gpr") {
            artifactId = "arbr-platform-base"
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


/**
 * jOOQ CodeGen from Database Schema
 */
jooq {
    version.set("$jooqVersion")
    edition.set(nu.studer.gradle.jooq.JooqEdition.OSS)

    val pgHost = project.properties["jooq.postgres.host"] as? String
        ?: System.getenv("DOCKER_HOST")
        ?: "localhost"
    val pgPort = System.getenv("DB_PORT")
        ?: project.properties["jooq.postgres.port"] as? String
        ?: "5433"
    val pgDatabase = (project.properties["jooq.postgres.database"] as? String) ?: ""
    val pgUser = (project.properties["jooq.postgres.user"] as? String) ?: ""
    val pgPassword = (project.properties["jooq.postgres.password"] as? String) ?: ""

    configurations {
        create("main") {  // name of the jOOQ configuration
            generateSchemaSourceOnCompilation.set(false)

            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = "jdbc:postgresql://$pgHost:$pgPort/$pgDatabase"
                    user = pgUser
                    password = pgPassword
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"

                        schemata = listOf(
                            SchemaMappingType().withInputSchema("public"),
                            SchemaMappingType().withInputSchema("datasets"),
                        )
                        forcedTypes.add(
                            ForcedType().apply {
                                // Specify the Java type of your custom type. This corresponds to the Binding's <U> type.
                                userType = "com.arbr.db.binding.Vector1536"

                                // Associate that custom type with your binding.
                                binding = "com.arbr.db.binding.Vector1536Binding"

                                // A Java regex matching fully-qualified columns, attributes, parameters. Use the pipe to separate several expressions.
                                // includeExpression = "embedding|array_to_vector"

                                // A Java regex matching data types to be forced to
                                // have this type.
                                //
                                // Data types may be reported by your database as:
                                // - NUMBER              regexp suggestion: NUMBER
                                // - NUMBER(5)           regexp suggestion: NUMBER\(5\)
                                // - NUMBER(5, 2)        regexp suggestion: NUMBER\(5,\s*2\)
                                // - any other form
                                //
                                // It is thus recommended to use defensive regexes for types.
                                //
                                // If provided, both "includeExpressions" and "includeTypes" must match.
                                includeTypes = ".*vector.*"
                            }
                        )
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                        isKotlinNotNullPojoAttributes = true
                        isKotlinNotNullInterfaceAttributes = true
                        isKotlinNotNullRecordAttributes = true
                    }
                    target.apply {
                        packageName = "com.arbr.db"
                        directory = "src/main/jooq"
                    }
                    strategy.name = "org.jooq.codegen.DefaultGeneratorStrategy"
                }
            }
        }
    }
}

buildscript {
    configurations["classpath"].resolutionStrategy.eachDependency {
        if (requested.group == "org.jooq") {
            useVersion("3.18.5")
        }
    }
}

/**
 * ANTLR Parser CodeGen from G4 Grammars
 */
val langRootRelDir = "src/main/java/com/topdown/parsers/lang"
val langDir: Path = Paths.get(project.projectDir.toPath().toString(), langRootRelDir)

val languageNames = Files.list(langDir)
    .filter { it.isDirectory() && it.name != "universal" }
    .toList()
    .map { p ->
        p.name to (p.name.let { n ->
            n.take(1).uppercase() + n.drop(1)
        })
    }

for ((lang, langTitle) in languageNames) {
    tasks.create<AntlrTask>("generate${langTitle}GrammarSource") {
        this.source = fileTree(
            "src/main/antlr/$lang"
        )
        this.include("*.g4")

        arguments = arguments + listOf(
            "-visitor",
            "-package",
            "com.topdown.parsers.lang.$lang.base",
        )

        outputDirectory = Paths.get(langDir.toString(), "$lang/base/").toFile()
    }
}

tasks.generateGrammarSource {
    // This makes the base generate task do nothing - each language is handled by a custom task
    this.source = fileTree("src/main/kotlin")
    this.include("*.g4")
}
tasks.generateTestGrammarSource {
    // This makes the base generate task do nothing - each language is handled by a custom task
    this.source = fileTree("src/main/kotlin")
    this.include("*.g4")
}

kotlin {
    jvmToolchain(21)
}

val generateLanguageSegmenters by tasks.creating(DefaultTask::class.java) {
    inputs.files(tasks.generateGrammarSource.get().outputs.files)
    for ((_, langTitle) in languageNames) {
        val name = "generate${langTitle}GrammarSource"
        inputs.files(tasks.getByName(name).outputs.files)
    }

//    dependsOn(tasks.generateGrammarSource)
//    for ((_, langTitle) in languageNames) {
//        val name = "generate${langTitle}GrammarSource"
//        dependsOn(tasks.getByName(name))
//    }
    doLast {
        ParserCodegen().generateParserLanguageSegmenters()
    }
    outputs.dir(project.layout.projectDirectory.dir("src/main/java/com/topdown/parsers/lang"))
}

tasks.withType<KotlinCompile> {
    inputs.files(generateLanguageSegmenters.outputs.files)
    dependsOn(tasks.generateGrammarSource)
    for ((_, langTitle) in languageNames) {
        val name = "generate${langTitle}GrammarSource"
        dependsOn(tasks.getByName(name))
    }
    dependsOn(generateLanguageSegmenters)
}
tasks.compileTestKotlin {
    dependsOn(tasks.generateTestGrammarSource)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<Jar> {
    dependsOn(tasks.generateGrammarSource)
    for ((_, langTitle) in languageNames) {
        val name = "generate${langTitle}GrammarSource"
        dependsOn(tasks.getByName(name))
    }
    dependsOn(generateLanguageSegmenters)

    inputs.files(generateLanguageSegmenters.outputs.files)

    archiveBaseName = "platform-base"
    archiveVersion = ""
}
tasks.getByName("kotlinSourcesJar") {
    dependsOn(tasks.generateGrammarSource)
    for ((_, langTitle) in languageNames) {
        val name = "generate${langTitle}GrammarSource"
        dependsOn(tasks.getByName(name))
    }
    dependsOn(generateLanguageSegmenters)
}


/**
 * Hacky parser codegen routine
 */
class ParserCodegen {
    private fun generate(targetFilePath: Path, baseName: String, titleName: String) {
        println("Generating for $titleName to $targetFilePath")
        val templatePath =
            project.layout.projectDirectory.file("src/main/resources/LangSegmenter.kttmpl").asFile.toPath()
        val templateText = templatePath.readText()

        val rootContexts = mapOf(
            "ts" to "program",
            "adaptive" to "query",
            "regex" to "root",
            "antlr4" to "grammarSpec",
        )

        val rootContext = rootContexts[baseName]
            ?: throw Exception("No root context defined for $baseName")

        val filledTemplate = templateText
            .replace("{%lang_id%}", baseName)
            .replace("{%lang_title%}", titleName)
            .replace("{%lang_root_context%}", rootContext)

        Files.createDirectories(
            Paths.get(
                "./",
                *targetFilePath.toList().dropLast(1).map { it.toString() }.toTypedArray(),
            )
        )
        targetFilePath.writeText(filledTemplate.trim() + "\n")
    }

    private fun titleCase(baseName: String): String {
        return baseName.split("_", " ").joinToString("") { it.take(1).uppercase() + it.drop(1).lowercase() }
    }

    fun generateParserLanguageSegmenters() {
        val nameOverrides = mapOf(
            "jsx" to "JavaScript",
            "css3" to "Css",
            "ts" to "TypeScript",
        )

        val langSourceDir =
            project.layout.projectDirectory.dir("src/main/java/com/topdown/parsers/lang").asFile.toPath()
        val languageNames = Files.list(langSourceDir)
            .filter { it.isDirectory() && it.name != "universal" }
            .toList()
            .map { p ->
                p.name to (p.name.let { n ->
                    nameOverrides.getOrDefault(n, titleCase(n))
                })
            }

        val targets = mutableListOf<() -> Unit>()
        for ((baseName, titleName) in languageNames) {
            val fileName = "${titleName}Segmenter.kt"
            val targetFile = Paths.get(langSourceDir.toString(), baseName, "segmenter", fileName)

            if (targetFile.exists()) {
                // TODO: Allow re-generating via flag or environment variable
                //
            } else {
                targets.add {
                    generate(targetFile, baseName, titleName)
                }
            }
        }

        for (target in targets) {
            target()
        }
    }
}
