package com.arbr.og.object_model.common.model

import com.arbr.object_model.core.types.naming.NamedResourceKey

interface ResourceIdentifier {
    val resourceKey: NamedResourceKey
    val resourceUuid: String
}