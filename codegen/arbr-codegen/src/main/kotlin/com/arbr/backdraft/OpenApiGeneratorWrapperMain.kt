package com.arbr.backdraft

import org.openapitools.codegen.OpenAPIGenerator
import org.slf4j.LoggerFactory
import java.io.PrintStream

class OpenApiGeneratorWrapperMain

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger(OpenApiGeneratorWrapperMain::class.java)
    val argString = StringBuilder().apply {
        for (arg in args) {
            append(" ")
            append(arg)
        }
        toString()
    }
    logger.info("Running org.openapitools.codegen.OpenAPIGenerator with args: $argString")

    // Redirect non-logback outputs from the library
    System.setOut(PrintStream(PrintStream.nullOutputStream()))
    OpenAPIGenerator.main(args)
}
