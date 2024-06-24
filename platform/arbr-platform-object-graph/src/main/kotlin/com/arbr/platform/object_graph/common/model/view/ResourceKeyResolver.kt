package com.arbr.og.object_model.common.model.view

import com.arbr.object_model.core.types.naming.NamedResource
import com.arbr.object_model.core.types.naming.NamedResourceKey

interface ResourceKeyResolver<RK: NamedResourceKey> {

    fun resolveKey(
        resourceKey: RK,
    ): NamedResource<*, RK, *, *>
}