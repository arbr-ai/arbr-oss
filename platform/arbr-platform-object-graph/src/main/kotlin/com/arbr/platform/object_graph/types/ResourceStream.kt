package com.arbr.platform.object_graph.types

import com.arbr.platform.object_graph.types.GeneralResource

/**
 * FKA ObjectModelResource
 */
interface ResourceStream<R: GeneralResource> {
    val uuid: String
}
