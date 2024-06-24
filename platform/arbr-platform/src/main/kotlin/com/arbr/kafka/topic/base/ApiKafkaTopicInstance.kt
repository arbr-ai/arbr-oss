package com.arbr.kafka.topic.base

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

/**
 * Topic instance = topic + key
 * A key should correspond to an instance of a stream within a topic, so that events on that stream instance go to the
 * same partition and therefore have strong order.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class ApiKafkaTopicInstance<K, T>(
    val key: String,
    val topicName: String,
    val topicVersionId: String,
    val objectKind: String,
    val objectClassFqn: String,
    val objectSchemaVersion: String,
    val objectKey: K,
    val objectValue: T,
)
