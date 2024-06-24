package com.arbr.ml.search

class ContractionInverseOperator<E: Any>(
    val inner: Contraction<E>,
): Contraction<E> {

    override fun getCode(encoder: ContractionEncoder<E>): Int {
        return -inner.getCode(encoder)
    }

    override fun combine(other: Contraction<E>): Contraction<E> {
        return ContractionOperator(
            this,
            other,
        )
    }

    override fun invert(): Contraction<E> {
        return inner
    }
}