package com.arbr.ml.search

import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.platform.ml.linear.typed.base.Matrix

/**
 * Ring element, factorization of a contraction
 */
data class ContractionBasis<E: Any>(
    val basis: Map<Int, Contraction<E>>,
)

/**
 * ContractionLattice provides the framework for maintaining a state as a set of group elements
 * Contractions as operations on the tokenization scheme are non-commutative in general, for example "ab" would not
 * commute with "bc" if and only if "abc" exists somewhere.
 *
 */
class ContractionLattice<E: Any, N: Dim, D: Dim>(
    private val document: List<E>,
    private val encoder: ContractionEncoder<E>,
    private val numAtomicElements: Int, // e.g. vocabulary size

    /**
     * N x D matrix where each row is a distribution on some target R.V.
     */
    private val distributions: Matrix<N, D>,

    /**
     * Computation of a cost metric (entropy) for some tokenization scheme, i.e. a contraction basis
     */
    private val evaluator: (ContractionBasis<E>) -> Double,

) {
    private val identity = ContractionIdentity<E>()
    private val atoms = (0..<numAtomicElements).map { i ->
        ContractionAtomicElement(encoder.decode(i))
    }

    /**
     * Running set of states
     */
    private val states = mutableMapOf<Int, Contraction<E>>()

    init {
        states[identity.getCode(encoder)] = identity

        // (a, b) = |a->b| - |ab|
        var prevElement: E? = null
        for (element in document) {

        }

        for (atom in atoms) {
            states[atom.getCode(encoder)] = atom
        }
    }

    fun iterate() {
        for ((code, contraction) in states.entries) {
            for ((otherCode, otherContraction) in states.entries) {

            }
        }
    }


}














