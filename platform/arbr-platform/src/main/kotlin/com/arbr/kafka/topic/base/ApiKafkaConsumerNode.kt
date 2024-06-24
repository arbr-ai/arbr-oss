package com.arbr.kafka.topic.base

import com.arbr.kafka.topic.config.ApiVersionedKafkaTopic
import reactor.core.publisher.Flux

interface ApiKafkaConsumerNode<K, T> {
    val topic: ApiVersionedKafkaTopic<K, T>

    fun receive(
        topicKey: K,
        offset: Long?,
        groupId: String?,
    ): Flux<ApiKafkaConsumerEvent<K, T>>

    fun receive(
        topicKey: K,
        offset: Long?,
    ): Flux<ApiKafkaConsumerEvent<K, T>> {
        return receive(
            topicKey = topicKey,
            offset = offset,
            groupId = null,
        )
    }

    fun receive(
        topicKey: K,
        groupId: String?,
    ): Flux<ApiKafkaConsumerEvent<K, T>> {
        return receive(
            topicKey = topicKey,
            offset = null,
            groupId = groupId,
        )
    }

    fun receive(topicKey: K): Flux<ApiKafkaConsumerEvent<K, T>> {
        return receive(
            topicKey = topicKey,
            offset = null,
            groupId = null,
        )
    }
}
