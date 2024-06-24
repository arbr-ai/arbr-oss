package com.arbr.kafka.topic.base

interface ApiKafkaTopicNodeFactory {
    /**
     * Procure a consumer node for the contextual type if it exists, or else null.
     */
    fun <K, T> consumerNode(messageObjectClass: Class<T>): ApiKafkaConsumerNode<K, T>?

    /**
     * Procure a producer node for the contextual type if it exists, or else null.
     */
    fun <K, T> producerNode(messageObjectClass: Class<T>): ApiKafkaProducerNode<K, T>?
}
