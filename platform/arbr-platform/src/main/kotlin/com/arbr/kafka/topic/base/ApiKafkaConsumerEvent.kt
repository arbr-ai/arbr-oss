package com.arbr.kafka.topic.base

data class ApiKafkaConsumerEvent<K, T>(
    val key: String,
    val offset: Long,
    val messageObject: ApiKafkaTopicInstance<K, T>
)
