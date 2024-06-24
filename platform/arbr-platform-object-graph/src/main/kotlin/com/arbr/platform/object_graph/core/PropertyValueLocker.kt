package com.arbr.platform.object_graph.core

import com.arbr.og.object_model.common.model.PropertyIdentifier

interface PropertyValueLocker {

    fun acquireRead(proposedValueStreamIdentifier: PropertyIdentifier)
    fun acquireWrite(proposedValueStreamIdentifier: PropertyIdentifier)
    fun releaseAll()

    companion object {
        val noOpLocker = object : PropertyValueLocker {
            override fun acquireRead(proposedValueStreamIdentifier: PropertyIdentifier) {
                // do nothing
            }

            override fun acquireWrite(proposedValueStreamIdentifier: PropertyIdentifier) {
                // do nothing
            }

            override fun releaseAll() {
                // do nothing
            }

        }
    }

}