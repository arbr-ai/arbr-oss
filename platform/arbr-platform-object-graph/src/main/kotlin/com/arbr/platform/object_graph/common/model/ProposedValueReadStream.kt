package com.arbr.og.object_model.common.model

interface ProposedValueReadStream<S : Any> : ProposedValueReadStreamBase<S> {

    val identifier: PropertyIdentifier

    fun getLatestAcceptedValue(): S?

    fun getLatestValue(): S?

}

