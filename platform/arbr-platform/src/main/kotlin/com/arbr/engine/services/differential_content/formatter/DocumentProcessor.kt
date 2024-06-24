package com.arbr.engine.services.differential_content.formatter

fun interface DocumentProcessor<DocumentType> {

    fun process(documentType: DocumentType): DocumentType
}

