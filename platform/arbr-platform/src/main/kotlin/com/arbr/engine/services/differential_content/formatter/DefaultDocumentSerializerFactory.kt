package com.arbr.engine.services.differential_content.formatter

import com.arbr.content_formats.format.DiffLiteralSourceDocument
import com.arbr.content_formats.format.DiffLiteralSourceDocumentSerializer
import com.arbr.content_formats.format.DiffOperation
import com.arbr.content_formats.format.tokenizer.TokenizationSerializer
import com.arbr.engine.util.FileContentUtils
import com.arbr.model_loader.indents.IndentAlignmentService
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@ComponentScan("com.arbr.model_loader.indents")
class DefaultDocumentSerializerFactory(
    private val indentAlignmentService: IndentAlignmentService,
    private val jsonLenientPostProcessor: JsonLenientPostProcessor,
) : DocumentSerializerFactory<DiffLiteralSourceDocument, DiffOperation> {
    private val diffLiteralSourceDocumentSerializer = DiffLiteralSourceDocumentSerializer()

    private fun getProcessors(
        workflowHandleId: String,
        filePath: String,
    ): List<DocumentProcessor<DiffLiteralSourceDocument>> {
        return if (FileContentUtils.fileIsPackageJson(filePath)) {
            listOf(jsonLenientPostProcessor)
        } else {
            emptyList()
        }
    }

    private fun combineProcessors(
        serializer: TokenizationSerializer<DiffLiteralSourceDocument, DiffOperation>,
        processors: List<DocumentProcessor<DiffLiteralSourceDocument>>,
    ): TokenizationSerializer<DiffLiteralSourceDocument, DiffOperation> {
        return TokenizationSerializer { tokens, formatter ->
            val serialized = serializer.serializeWith(tokens, formatter)

            processors.fold(serialized) { a, p ->
                p.process(a)
            }
        }
    }

    override fun getCombinedDocumentSerializer(
        workflowHandleId: String,
        filePath: String,
    ): Mono<TokenizationSerializer<DiffLiteralSourceDocument, DiffOperation>> {
        val formatterCompilerMono = indentAlignmentService
            .getDocumentFormatterCompiler(
                workflowHandleId,
                filePath,
            )

        return formatterCompilerMono.map { formatterCompiler ->
            val combinedSerializer = Formatters.combine(
                diffLiteralSourceDocumentSerializer,
                listOf(
                    formatterCompiler,
                )
            )

            val postProcessors = getProcessors(
                workflowHandleId,
                filePath,
            )
            combineProcessors(combinedSerializer, postProcessors)
        }
    }
}
