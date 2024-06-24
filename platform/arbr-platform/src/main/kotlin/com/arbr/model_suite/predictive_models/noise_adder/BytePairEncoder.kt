package com.arbr.model_suite.predictive_models.noise_adder

import kotlin.math.min

class BytePairEncoder private constructor(
    private val vocab: Set<String>,
    private val vocabSizeDivisor: Int,
    private val frequencies: Map<Pair<BPEToken, BPEToken>, Int>,
    private val delimiters: List<String>,
) {
    private val maxVocabLength = vocab.maxOfOrNull { it.length } ?: 0

    class TrieNode(val children: MutableMap<Char, TrieNode> = mutableMapOf(), var isWord: Boolean = false)

    private val trieRoot = TrieNode()

    init {
        buildTrie()
    }

    private fun buildTrie() {
        for (word in vocab) {
            var current = trieRoot
            for (char in word) {
                current = current.children.getOrPut(char) { TrieNode() }
            }
            current.isWord = true
        }
    }

    private fun longestWordInTrie(baseTokens: List<String>, fromIndex: Int): String? {
        var current = trieRoot
        var longestMatch: String? = null

        for (j in fromIndex until min(fromIndex + maxVocabLength, baseTokens.size)) {
            val tokenWord = baseTokens[j]

            var didFind = true
            for (char in tokenWord) {
                val next = current.children[char]
                if (next == null) {
                    didFind = false
                } else {
                    current = next
                }
            }

            if (!didFind) {
                break
            }

            if (current.isWord) {
                longestMatch = baseTokens.subList(fromIndex, j + 1).joinToString(separator = "")
            }
        }

        return longestMatch
    }

    private fun encode(baseTokens: List<String>): List<String> {
        val encodedWords = mutableListOf<String>()
        var i = 0

        while (i < baseTokens.size) {
            val longestMatch = longestWordInTrie(baseTokens, i)

            if (longestMatch == null) {
                encodedWords.add(baseTokens[i])
                i++
            } else {
                encodedWords.add(longestMatch)
                i += longestMatch.length
            }
        }

        return encodedWords
    }

    fun encode(input: String): List<String> {
        val baseTokens = tokenizeBase(input, delimiters)
        return encode(baseTokens)
    }

    fun extendTraining(documents: List<String>, delimiters: List<String> = defaultDelimiters): BytePairEncoder {
        return trainFrom(
            documents,
            vocabSizeDivisor,
            delimiters,
            frequencies.toMutableMap(),
        )
    }

    companion object {
        val defaultDelimiters = "<>(){}[] \n\";,|=.".map { it.toString() }

        private sealed class BPEToken(
            open val value: String,
            open val minIndexInclusive: Int,
            open val maxIndexInclusive: Int,
        ) {
            data class Original(
                override val value: String,
                val index: Int,
            ) : BPEToken(value, index, index) {
                override fun equals(other: Any?): Boolean {
                    return (other != null) && (other is Original) && (value == other.value)
                }

                override fun hashCode(): Int {
                    return hashOffset + value.hashCode()
                }

                companion object {
                    private val hashOffset = "Original".hashCode() * 41
                }
            }

            data class Replacement(
                override val value: String,
                override val minIndexInclusive: Int,
                override val maxIndexInclusive: Int,
            ) : BPEToken(value, minIndexInclusive, maxIndexInclusive) {
                override fun equals(other: Any?): Boolean {
                    return (other != null) && (other is Replacement) && (value == other.value)
                }

                override fun hashCode(): Int {
                    return hashOffset + value.hashCode()
                }

                companion object {
                    private val hashOffset = "Replacement".hashCode() * 31
                }
            }

            object End : BPEToken("|EOF|", -1, -1)
        }

        private fun pairFrequency(
            tokens: List<BPEToken>,
            frequency: MutableMap<Pair<BPEToken, BPEToken>, Int>
        ): MutableMap<Pair<BPEToken, BPEToken>, Int> {
            for ((i, token) in tokens.withIndex()) {
                if (i < tokens.size - 1) {
                    val nextToken = tokens[i + 1]
                    frequency.compute(token to nextToken) { _, v ->
                        (v ?: 0) + 1
                    }
                } else {
                    frequency[token to BPEToken.End] = 1
                }
            }
            return frequency
        }

        private fun tokenizeBase(
            text: String,
            delimiters: List<String>,
        ): List<String> {
            if (text.isBlank()) {
                return listOf(text)
            }
            var tokens = listOf(text)
            for (delimiter in delimiters) {
                tokens = tokens.flatMap {
                    it.split(delimiter).flatMap { tk ->
                        listOf(tk, delimiter)
                    }.dropLast(1)
                        .filter { t -> t.isNotEmpty() }
                }
            }

            return tokens
        }

        private fun trainFrom(
            documents: List<String>,
            vocabSizeDivisor: Int,
            delimiters: List<String>,
            baseFrequency: MutableMap<Pair<BPEToken, BPEToken>, Int>,
        ): BytePairEncoder {
            var tokens: List<BPEToken> = documents
                .flatMap { tokenizeBase(it, delimiters) }
                .mapIndexed { i, c -> BPEToken.Original(c, i) }
            val vocabSize = tokens.size / vocabSizeDivisor

            val frequency = pairFrequency(tokens, baseFrequency)
            var vocab = tokens.map { it.value }.toSet()

            while (vocab.size > vocabSize) {
                val maxFrequency = frequency.maxByOrNull { it.value } ?: break
                if (maxFrequency.value < 2) {
                    break
                }

                val mostFrequentPair = maxFrequency.key
                frequency.remove(mostFrequentPair)

                var lastCombineIndex: Int? = null
                tokens = tokens.mapIndexedNotNull { index, bpeToken ->
                    val rightToken = if (index < tokens.size - 1) tokens[index + 1] else null

                    if (lastCombineIndex == index - 1) {
                        null
                    } else if (rightToken != null && (bpeToken to rightToken) == mostFrequentPair) {
                        lastCombineIndex = index
                        val combinedToken = BPEToken.Replacement(
                            bpeToken.value + rightToken.value,
                            bpeToken.minIndexInclusive,
                            rightToken.maxIndexInclusive,
                        )
                        if (index < tokens.size - 2) {
                            val rightRightToken = tokens[index + 2]
                            frequency.compute(rightToken to rightRightToken) { _, v ->
                                if (v != null && v > 1) {
                                    v - 1
                                } else {
                                    null
                                }
                            }
                            frequency.compute(combinedToken to rightRightToken) { _, v ->
                                (v ?: 0) + 1
                            }
                        }
                        if (index > 0) {
                            val leftToken = tokens[index - 1]
                            frequency.compute(leftToken to bpeToken) { _, v ->
                                if (v != null && v > 1) {
                                    v - 1
                                } else {
                                    null
                                }
                            }
                            frequency.compute(leftToken to combinedToken) { _, v ->
                                (v ?: 0) + 1
                            }
                        }

                        combinedToken
                    } else {
                        bpeToken
                    }
                }

                vocab = tokens.map { it.value }.toSet()
            }

            return BytePairEncoder(vocab, vocabSizeDivisor, frequency, delimiters)
        }

        fun train(document: String, vocabSizeDivisor: Int, delimiters: List<String> = defaultDelimiters): BytePairEncoder {
            return trainFrom(listOf(document), vocabSizeDivisor, delimiters, mutableMapOf())
        }
    }
}
