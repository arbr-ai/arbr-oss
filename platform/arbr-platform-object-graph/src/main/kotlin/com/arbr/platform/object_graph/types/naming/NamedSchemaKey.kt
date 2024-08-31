package com.arbr.platform.object_graph.types.naming

import com.arbr.platform.object_graph.types.suites.EnumLike

interface NamedSchemaKey: EnumLike {
    override val name: String
    override val ordinal: Int
}
