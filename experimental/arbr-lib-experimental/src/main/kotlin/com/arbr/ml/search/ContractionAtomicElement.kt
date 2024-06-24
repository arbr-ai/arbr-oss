package com.arbr.ml.search

class ContractionAtomicElement<E: Any>(
    val element: E,
): Contraction<E> {
    override fun getCode(encoder: ContractionEncoder<E>): Int {
        return encoder.encode(element)
    }

    override fun combine(other: Contraction<E>): Contraction<E> {
        return other
    }

    override fun invert(): Contraction<E> {
        return this
    }
}