package com.arbr.kafka.topic.config

import com.arbr.kafka.topic.base.ApiKafkaTopicInstance

sealed interface ApiVersionedKafkaTopic<K, T> {
    val messageObjectKeyClass: Class<K>
    val messageObjectClass: Class<T>
    val topicInstanceClass: Class<ApiKafkaTopicInstance<K, T>>

    val versionId: String

    fun objectSpecificId(modelObject: T): String

    fun topicName(topicKey: K): String
}
