package com.arbr.engine.services.workflow.state

data class WorkflowConsumption(
    val gpt4Usage: Long,
    val gpt35t16kUsage: Long,
    val gpt35tUsage: Long,
    val unknownUsage: Long,
)