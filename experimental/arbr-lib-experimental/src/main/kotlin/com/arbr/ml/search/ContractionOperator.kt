package com.arbr.ml.search

class ContractionOperator<E: Any>(
    val left: Contraction<E>,
    val right: Contraction<E>,
): Contraction<E> {
    override fun getCode(encoder: ContractionEncoder<E>): Int {
        return encoder.combine(left.getCode(encoder), right.getCode(encoder))
    }

    override fun combine(other: Contraction<E>): Contraction<E> {
        return ContractionOperator(
            this,
            other,
        )
    }

    override fun invert(): Contraction<E> {
        return ContractionInverseOperator(this)
    }
}
