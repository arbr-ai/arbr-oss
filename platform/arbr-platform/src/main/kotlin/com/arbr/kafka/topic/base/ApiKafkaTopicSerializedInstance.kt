package com.arbr.kafka.topic.base

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ApiKafkaTopicSerializedInstance(
    val key: String,
    val topicName: String,
    val topicVersionId: String,
    val objectKind: String,
    val objectClassFqn: String,
    val objectSchemaVersion: String,
    val objectKey: Any,
    val objectValue: Map<String, Any>,
)
