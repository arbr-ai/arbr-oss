package com.arbr.graphql.converter

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import kotlin.system.exitProcess

class ConverterMain

fun main(args: Array<String>) {
    (LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger).level = Level.INFO

    val logger = LoggerFactory.getLogger(ConverterMain::class.java)
    logger.info("Running GraphQL lang converter...")
    logger.info("Args: ${args.toList()}")

    if (args.isEmpty()) {
        logger.error("Missing required positional argument for build output dir")
        exitProcess(1)
    }

    val buildDir = Paths.get(args[0])
    val routine = ConverterRoutine(buildDir)
    routine.run()
}
