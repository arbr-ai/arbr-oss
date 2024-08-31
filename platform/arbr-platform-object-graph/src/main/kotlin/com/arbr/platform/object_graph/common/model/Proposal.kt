package com.arbr.platform.object_graph.common.model

/**
 * A proposal whose acceptance should be delegated to an arbiter
 */
interface Proposal<S: Any> {

    val acceptedValue: S?

    val proposedValue: S?

    fun accept()

    fun reject()
}