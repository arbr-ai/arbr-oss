package com.arbr.engine.services.vector_db.model

import com.fasterxml.jackson.annotation.JsonValue

enum class Namespace(
    @JsonValue
    val namespaceName: String
) {
    CORE_EMBEDDING("core-embedding"),
    AI_APPLICATION_EXAMPLE("ai-application-example-3"),
    GITHUB_METADATA("github-metadata"),
    MARKOV_MODEL("markov-model"),
}