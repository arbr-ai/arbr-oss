package com.arbr.object_model.core.types.naming

import com.arbr.object_model.core.types.suites.EnumLike

interface NamedForeignKey: EnumLike {
    override val name: String
    override val ordinal: Int
}