package com.arbr.engine.services.differential_content.formatter

import com.arbr.content_formats.format.tokenizer.TokenizationSerializer
import reactor.core.publisher.Mono

interface DocumentSerializerFactory<DocumentType, TokenType> {

    fun getCombinedDocumentSerializer(
        workflowHandleId: String,
        filePath: String,
    ): Mono<TokenizationSerializer<DocumentType, TokenType>>
}
