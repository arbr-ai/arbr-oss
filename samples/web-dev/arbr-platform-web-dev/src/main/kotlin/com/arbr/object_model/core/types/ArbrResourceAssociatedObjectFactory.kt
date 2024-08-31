package com.arbr.platform.object_graph.types

import com.arbr.platform.object_graph.types.suites.ResourceAssociatedObjectCollection
import com.arbr.platform.object_graph.types.suites.ResourceAssociatedObjectCollectionBuilder

class ArbrResourceAssociatedObjectFactory {

    inline fun <reified T: Any> builder(): ResourceAssociatedObjectCollectionBuilder<ArbrResourceKey, T> {
        return Builder { t ->
            ResourceAssociatedObjectCollection.new(
                ArbrResourceKey.values(),
                t,
            )
        }
    }

    class Builder<T: Any>(
        private val curriedConstructor: ((ArbrResourceKey) -> T) -> ResourceAssociatedObjectCollection<ArbrResourceKey, T>,
    ): ResourceAssociatedObjectCollectionBuilder<ArbrResourceKey, T> {

        override fun buildWith(
            transform: (ArbrResourceKey) -> T
        ): ResourceAssociatedObjectCollection<ArbrResourceKey, T> {
            return curriedConstructor(transform)
        }
    }
}