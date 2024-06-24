package com.arbr.types.homotopy.spec

import com.arbr.types.homotopy.MutablePathContext

interface ContractionHomotopy<Trait> : NodeHomotopy<Trait>, PrimitiveHomotopy<Trait> {

    companion object {
        fun <Trait> default(): ContractionHomotopy<Trait> = object : ContractionHomotopy<Trait> {
            override fun liftNode(
                context: MutablePathContext,
                valueTypeImplementor: Trait,
                innerImplementors: List<Trait>
            ): Trait {
                return valueTypeImplementor
            }

        }
    }
}
