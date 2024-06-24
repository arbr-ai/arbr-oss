package com.arbr.ml.search

class ContractionIdentity<E: Any>: Contraction<E> {
    override fun getCode(encoder: ContractionEncoder<E>): Int {
        return encoder.identityCode()
    }

    override fun combine(other: Contraction<E>): Contraction<E> {
        return other
    }

    override fun invert(): Contraction<E> {
        return this
    }
}

