package com.arbr.kafka.topic.base

import com.arbr.kafka.topic.config.ApiVersionedKafkaTopic
import reactor.core.publisher.Mono

interface ApiKafkaProducerNode<K, T> {
    val topic: ApiVersionedKafkaTopic<K, T>

    fun send(
        messageKeyObject: K,
        messageObject: T
    ): Mono<Void>
}
