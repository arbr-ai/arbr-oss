package com.arbr.graphql.converter

import com.arbr.graphql.converter.model.MappedNodeClassModel
import com.arbr.graphql.converter.parser.GraphQlLangModelParser
import org.slf4j.LoggerFactory
import java.nio.file.Path

class ConverterRoutine(
    private val buildDir: Path
) {
    private val graphQlLangModelParser = GraphQlLangModelParser(buildDir)
    private val ktSourceConverter = LangToKtConverter(buildDir)
    private val graphQlSchemaConverter = LangToGraphQlSchemaConverter(buildDir)

    fun run() {
        logger.info("Will build into $buildDir")
        val parsedModels = graphQlLangModelParser.parseBaseNodeClassModels()
        val mappedModels: List<MappedNodeClassModel> = graphQlLangModelParser.mapBaseNodeModels(parsedModels)

        logger.info("Converting parsed models to Kt source...")
        ktSourceConverter.convert(mappedModels)

        logger.info("Converting parsed models to GraphQl schema...")
        graphQlSchemaConverter.convert(mappedModels)

        logger.info("Compiling transform utilities...")
        LangToKtTransformerCompiler(buildDir, mappedModels).compileTransformer()

        logger.info("Compiling visitor method utilities...")
        val transformerCompiler = LangToKtVisitorMethodCompiler(buildDir, mappedModels)
        transformerCompiler.compileTransformer()

        logger.info("Compiling type enumeration...")
        val enumCompiler = LangToKtEnumCompiler(buildDir, mappedModels)
        enumCompiler.compileEnumClass()

        logger.info("Compiling renderer...")
        LangToKtRendererCompiler(buildDir, mappedModels).compileRenderer()

        logger.info("Completed converter sequence!")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConverterRoutine::class.java)
    }
}