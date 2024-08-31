package com.arbr.platform.object_graph.common.model

import com.arbr.platform.object_graph.types.naming.NamedResourceKey

interface ResourceIdentifier {
    val resourceKey: NamedResourceKey
    val resourceUuid: String
}