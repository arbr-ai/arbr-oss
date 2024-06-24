package com.arbr.kafka.differential_models

import com.arbr.kafka.topic.base.ApiKafkaTopicNodeFactory

fun interface DifferentialModelSinkFactory<K, S, D> {

    fun makeDifferentialModelSink(
        nodeFactory: ApiKafkaTopicNodeFactory,
        binding: DifferentialModelBinding<K, S, D>
    ): DifferentialModelSink<K, S, D>
}
