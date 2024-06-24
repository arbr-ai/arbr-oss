package com.arbr.ml.search

sealed interface Contraction<E: Any> {
    /**
     * Unique code for the element within the group.
     */
    fun getCode(encoder: ContractionEncoder<E>): Int

    fun combine(other: Contraction<E>): Contraction<E>

    fun invert(): Contraction<E>
}