package com.arbr.platform.object_graph.common.model

interface ProposedValueReadStream<S : Any> : ProposedValueReadStreamBase<S> {

    val identifier: PropertyIdentifier

    fun getLatestAcceptedValue(): S?

    fun getLatestValue(): S?

}

