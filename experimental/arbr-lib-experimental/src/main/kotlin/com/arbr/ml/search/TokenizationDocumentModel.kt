package com.arbr.ml.search

import com.arbr.util_common.invariants.Invariants
import java.util.concurrent.ConcurrentHashMap

data class TokenizationContractionCandidate(
    val contraction: TokenCodeContraction,
    val numOccurrences: Int,
    val resultingDocument: TokenizationDocumentModel,
)

data class TokenizationBaseDocumentMetadata<T : Any, V : Any>(
    val tokenCodes: List<Int>,
    val tokenStrings: List<T>,
    val tokenFeatures: List<V>,
)

data class TokenRepr(
    val code: Int,
    val positivity: Int,
    val negativity: Int,
)

sealed interface TokenizationDocumentModelTreeNode {

    val numTokens: Int
    fun tokenAt(index: Int): Int

    fun tokenRepr(code: Int): TokenRepr //
    fun tokenCount(code: Int): Int
    fun tokenPositivity(code: Int): Int
    fun tokenNegativity(code: Int): Int

}

data class TokenizationDocumentModelTreeNodeRoot(
    private val tokenCodes: List<Int>,  // (i, k) -> token segment begins at index i in base with code k
    private val tokenPositivity: List<Int>,  // (i, k) -> token segment begins at index i in base with code k
    private val tokenNegativity: List<Int>,  // (i, k) -> token segment begins at index i in base with code k
) : TokenizationDocumentModelTreeNode {
    private val counts = tokenCodes.groupingBy { it }.eachCount()
    private val positivity = tokenCodes.zip(tokenPositivity).groupingBy { it.first }
        .fold(0) { r, p -> r + p.second }
    private val negativity = tokenCodes.zip(tokenNegativity).groupingBy { it.first }
        .fold(0) { r, p -> r + p.second }

    override val numTokens: Int
        get() = tokenCodes.size

    override fun tokenAt(index: Int): Int {
        return tokenCodes[index]
    }

    override fun tokenRepr(code: Int): TokenRepr {
        TODO("Not yet implemented")
    }

    override fun tokenCount(code: Int): Int {
        return counts[code]!!
    }

    override fun tokenPositivity(code: Int): Int {
        return positivity[code]!!
    }

    override fun tokenNegativity(code: Int): Int {
        return negativity[code]!!
    }

}

data class TokenizationDocumentModelTreeInteriorNode(
    val parent: TokenizationDocumentModelTreeNode,
    val contraction: TokenCodeContraction,
    val numOccurrences: Int,
    override val numTokens: Int
) : TokenizationDocumentModelTreeNode {
    private val countsCache = mutableMapOf<Int, Int>()
    private val positivityCache = mutableMapOf<Int, Int>()
    private val negativityCache = mutableMapOf<Int, Int>()

    override fun tokenAt(index: Int): Int {
        throw Exception()
    }

    override fun tokenRepr(code: Int): TokenRepr {
        throw Exception()
    }

    @Synchronized
    override fun tokenCount(code: Int): Int {
        if (code == contraction.code) {
            // Relies on the restriction that same contraction can't be applied more than once due to monotonic lengths
            return numOccurrences
        }

        return countsCache.computeIfAbsent(code) {
            val modifier = when (code) {
                contraction.leftCode -> -numOccurrences
                contraction.rightCode -> -numOccurrences
                else -> 0
            }

            parent.tokenCount(code) + modifier
        }
    }

    @Synchronized
    override fun tokenPositivity(code: Int): Int {
        return positivityCache.computeIfAbsent(code) {
            if (code == contraction.code) {
                Invariants.check {
                    require(contraction.leftCode != code)
                    require(contraction.rightCode != code)
                }
                tokenPositivity(contraction.leftCode) + tokenPositivity(contraction.rightCode)
            } else {
                val modifier = when (code) {
                    contraction.leftCode -> -numOccurrences
                    contraction.rightCode -> -numOccurrences
                    else -> 0
                }

                parent.tokenPositivity(code) + modifier
                parent.tokenPositivity(code) - numOccurrences
            }
        }
    }

    @Synchronized
    override fun tokenNegativity(code: Int): Int {
        return negativityCache.computeIfAbsent(code) {
            if (code == contraction.code) {
                Invariants.check {
                    require(contraction.leftCode != code)
                    require(contraction.rightCode != code)
                }
                tokenPositivity(contraction.leftCode) + tokenPositivity(contraction.rightCode)
            } else {
                parent.tokenNegativity(code)
            }
        }
    }
}

data class TokenizationDocumentModel(
    val node: TokenizationDocumentModelTreeNode,
    val latestContractionCode: Int,  // latest code
    val vocabulary: TokenVocabulary<String>,
) {

    fun pairings(minFrequency: Int): List<TokenizationContractionCandidate> {
        val pairingCounts = mutableMapOf<Int, Int>()
        val pairings = mutableMapOf<Int, TokenCodeContraction>()

//        var segmentIndex = 0
//        while (segmentIndex < node.numTokens - 1) {
//            val k0 = tokenCodes[segmentIndex]
//            val k1 = tokenCodes[segmentIndex + 1]
//
//            val contractionCode = vocabulary.encodePair(vocabulary.decode(k0), vocabulary.decode(k1))
//            pairingCounts.compute(contractionCode) { _, v -> (v ?: 0) + 1 }
//            pairings.computeIfAbsent(contractionCode) {
//                TokenCodeContraction(contractionCode, k0, k1)
//            }
//
//            segmentIndex++
//        }

        val frequentPairingCandidates = pairingCounts
            .filterValues { it >= minFrequency }
            .also {
                println("Evaluating ${it.size} contractions")
            }
            .map { (contractionCode, numOccurrences) ->
                val contraction = pairings[contractionCode]!!
                throw Exception()

//                val nextModel = TokenizationDocumentModel(
//                    TokenizationDocumentModelTreeInteriorNode(node, contraction, numOccurrences),
//
//                    )
//
//                TokenizationContractionCandidate(
//                    pairings[contractionCode]!!,
//                    numOccurrences,
//                    nextModel,
//                )
            }

        return frequentPairingCandidates
    }

    companion object {

        fun fromBaseDocument(
            baseDocument: List<TaggedToken>,
        ): TokenizationDocumentModel {
            val vocabulary = mutableListOf<String>()
            for (taggedToken in baseDocument) {
                if (taggedToken.token !in vocabulary) {
                    vocabulary.add(taggedToken.token)
                }
            }
            val vocabularyEncoder = vocabulary.withIndex()
                .associate { it.value to it.index }
                .toMutableMap()
            val vocabularyDecoder = vocabularyEncoder
                .entries
                .associate { it.value to it.key }
                .toMutableMap()

            val baseDocumentCodes = mutableListOf<Int>()
            val baseDocumentPositivity = mutableListOf<Int>()
            val baseDocumentNegativity = mutableListOf<Int>()

            for (taggedToken in baseDocument) {
                val code = vocabularyEncoder[taggedToken.token]!!
                baseDocumentCodes.add(code)
                baseDocumentPositivity.add(taggedToken.positivity)
                baseDocumentNegativity.add(taggedToken.negativity)
            }

            throw Exception()

//            return TokenizationDocumentModel(
//                parent = null,
//                tokenCodes = baseDocumentCodes,
//                segmentPositivity = baseDocumentPositivity,
//                segmentNegativity = baseDocumentNegativity,
//                tokenCounts = baseDocumentCodes.groupingBy { it }.eachCount(),
//                tokenPositivity = baseDocumentCodes.zip(baseDocumentPositivity).groupingBy { it.first }
//                    .fold(0) { r, p -> r + p.second },
//                tokenNegativity = baseDocumentCodes.zip(baseDocumentNegativity).groupingBy { it.first }
//                    .fold(0) { r, p -> r + p.second },
//                contractions = listOf(
//                    TokenCodeContraction(0, 0, 0),
//                ),
//                encoder = ConcurrentHashMap(vocabularyEncoder),
//                decoder = ConcurrentHashMap(vocabularyDecoder),
//            )
        }
    }
}