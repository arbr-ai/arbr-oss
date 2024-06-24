package com.arbr.graphql.lang.parser

import com.arbr.graphql_compiler.core.SchemaSource
import graphql.language.Document
import graphql.parser.InvalidSyntaxException
import graphql.parser.MultiSourceReader
import graphql.parser.Parser
import graphql.parser.ParserOptions
import org.slf4j.LoggerFactory
import java.io.File

class SchemaDocumentLoader {

    private fun loadSchemaReaders(
        schemaSources: List<SchemaSource>,
        readerBuilders: List<MultiSourceReader.Builder>
    ) {
        val sourcedSchemaFiles: List<Pair<SchemaSource, File>> = schemaSources.flatMap { source ->
            source.getSchemaPaths()
                .sorted()
                .map { path ->
                    source to path.toFile()
                }
                .toList()
        }

        readerBuilders.forEach { rb ->
            sourcedSchemaFiles.forEach { (_, schemaFile) ->
                // TODO: Allow specifying schema source name
//                val sourceName = schemaFile.nameWithoutExtension

                rb.string("\n", "codegen")
                rb.reader(schemaFile.reader(), schemaFile.name)
            }
        }
    }

    /**
     * Load the combined document from the schema sources
     */
    fun loadDocuments(
        schemaSources: List<SchemaSource>,
    ): Document {
        val options = ParserOptions.getDefaultParserOptions().transform { builder ->
            builder.maxTokens(SDL_MAX_ALLOWED_SCHEMA_TOKENS).maxWhitespaceTokens(SDL_MAX_ALLOWED_SCHEMA_TOKENS)
        }
        val parser = Parser()

        val readerBuilder = MultiSourceReader.newMultiSourceReader()
        val debugReaderBuilder = MultiSourceReader.newMultiSourceReader()
        val readerBuilders = listOf(readerBuilder, debugReaderBuilder)

        loadSchemaReaders(schemaSources, readerBuilders)

        val document = readerBuilder.build().use { reader ->
            try {
                parser.parseDocument(reader, options)
            } catch (exception: InvalidSyntaxException) {
                // check if the schema is empty
                val sourcePreview = try {
                    exception.sourcePreview ?: null
                } catch (e: NullPointerException) {
                    null
                }

                // check if the schema is empty
                if (sourcePreview?.trim() == "") {
                    logger.warn("Combined schema is empty")
                    // return an empty document
                    return Document.newDocument().build()
                } else {
                    // Use the debug reader to equip the error message with some extra detail
                    throw SchemaDocumentParsingException(debugReaderBuilder.build(), exception)
                }
            }
        }
        return document
    }

    companion object {
        private const val SDL_MAX_ALLOWED_SCHEMA_TOKENS: Int = Int.MAX_VALUE
        private val logger = LoggerFactory.getLogger(SchemaDocumentLoader::class.java)
    }

}

