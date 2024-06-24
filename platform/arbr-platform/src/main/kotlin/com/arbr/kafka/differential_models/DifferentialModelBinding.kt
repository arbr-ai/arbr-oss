package com.arbr.kafka.differential_models

import com.arbr.kafka.topic.base.ApiKafkaTopicNodeFactory

interface DifferentialModelBinding<K, S, D> {
    val modelClass: Class<S>
    val diffClass: Class<D>

    val initialState: DifferentialModelTrackedState<S>
    val splitter: DifferentialModelSplitter<S, D>
    val reducer: DifferentialModelReducer<S, D>

    val sinkFactory: DifferentialModelSinkFactory<K, S, D>

    fun makeDifferentialModelSink(
        nodeFactory: ApiKafkaTopicNodeFactory,
    ): DifferentialModelSink<K, S, D> {
        return sinkFactory.makeDifferentialModelSink(nodeFactory, this)
    }
}
