package com.arbr.platform.object_graph.common.model.view

import com.arbr.platform.object_graph.types.naming.NamedResource
import com.arbr.platform.object_graph.types.naming.NamedResourceKey

interface ResourceKeyResolver<RK: NamedResourceKey> {

    fun resolveKey(
        resourceKey: RK,
    ): NamedResource<*, RK, *, *>
}