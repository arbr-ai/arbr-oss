package com.arbr.types.homotopy.htype

import com.arbr.types.homotopy.keys.CompoundKeyPathPropertyKey

class PrimitiveHType<Tr, F : Tr>(
    override val keyPath: CompoundKeyPathPropertyKey,
    val baseTypeRepresentation: F,
) : HType<Tr, F> {
    override val implementor: Tr = baseTypeRepresentation

    override fun toString(): String {
        return "Pr[key=\"${keyPath}\", base=\"${baseTypeRepresentation}\"]"
    }
}
