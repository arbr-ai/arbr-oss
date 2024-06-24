package com.arbr.engine.services.db.model

data class WorkflowRawConsumption(
    val workflowId: Long,
    val usedModel: String,
    val totalTokens: Long,
)