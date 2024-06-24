package com.arbr.backdraft

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.arbr.backdraft.task.executor.DefaultTaskExecutor
import com.arbr.codegen.config.ArbrSchemaTaskDefinitionConfigModel
import com.arbr.graphql_compiler.core.*
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.inf.ArgumentParserException
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.system.exitProcess

class TaskMain

fun main(args: Array<String>) {
    (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger).level = Level.DEBUG
    val logger = LoggerFactory.getLogger(TaskMain::class.java)

    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    logger.info("Program arguments: ${args.joinToString()}")

    val parser = ArgumentParsers.newFor("GraphQLCompiler").build()
        .defaultHelp(true)
        .description("Compile one or more GraphQL schema documents into a target form.")

    parser.addArgument("-t", "--target")
        .help("The target form for compilation (fully qualified class name).")

    parser.addArgument("-o", "--output")
        .required(false)
        .help("The directory to place artifacts of compilation.")

    // TODO: Support glob patterns
    // TODO: Support URIs to enable schemes like https, classpath, file
    parser.addArgument("schema")
        .nargs("*")
        .help("Schema file(s) to parse. If empty, scans the classpath for files with extension in [graphql, graphqls].")

    parser.addArgument("-c", "--config")
        .setDefault("")
        .help("The path to a json-formatted config file, superseding other configuration arguments")

    val ns = try {
        parser.parseArgs(args)
    } catch (e: ArgumentParserException) {
        parser.handleError(e)
        exitProcess(1)
    }

    val configFilePath = ns.getString("config")
    val inputConfigurations = if (!configFilePath.isNullOrBlank()) {
        val configFile = Paths.get(configFilePath)
        if (!configFile.exists()) {
            logger.error("Config file does not exist: $configFilePath")
            exitProcess(1)
        }
        val mapper = jacksonObjectMapper()
        val config = mapper.readValue(configFile.toFile(), ArbrSchemaTaskDefinitionConfigModel::class.java)
        if (config == null) {
            logger.error("Empty config specified at $configFilePath")
            exitProcess(1)
        }

        listOf(
            TaskCommandLineArguments(
                config.name,
                config.target,
                config.outputBaseDir,
                config.sourceFiles,
            )
        )
    } else {
        val parsedLiteralArgs = TaskCommandLineArguments(
            "primary",
            ns.getString("target"),
            ns.getString("output"),
            ns.getList("schema"),
        )

        listOf(parsedLiteralArgs)
    }

    for (parsedLiteralArgs in inputConfigurations) {
        val schemaSourceResolver = SchemaSourceResolver()
        val schemaSources = schemaSourceResolver.loadFiles(parsedLiteralArgs.schemaSources)
        val schemaSourcesList = listOf(schemaSources)
        val executor = DefaultTaskExecutor(schemaSourcesList)

        executor.executeSingleTask(
            parsedLiteralArgs,
        )
    }
}
