package com.arbr.platform.object_graph.file_system

data class HeadlessDocumentResult(
    val content: String,
    val output: List<Map<String, Any>>,
)