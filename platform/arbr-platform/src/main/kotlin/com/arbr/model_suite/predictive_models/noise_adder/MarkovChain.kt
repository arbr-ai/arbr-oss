package com.arbr.model_suite.predictive_models.noise_adder

import org.apache.commons.text.similarity.LevenshteinDistance
import java.io.*
import java.util.*

data class MarkovChainSerializableEntry(
    val key: String,
    val tokens: List<String>,
    val targetDistribution: Map<String, Int>,
): Serializable

data class MarkovChainSerializableForm(
    val transitionDistributions: List<MarkovChainSerializableEntry>,
    val windowSize: Int,
): Serializable

class MarkovChain<T : Any> private constructor(
    transitionDistributions: Map<List<T>, DiscreteDist<T>>,
    val windowSize: Int,
) {
    private val transitionDists: Map<List<T>, DiscreteDist<T>> = transitionDistributions.toMap()
    private val transitionEntryMap: Map<String, MarkovChainSerializableEntry> = transitionDistributions
        .toList()
        .associate { (tokens, dist) ->
            val stringTokens = tokens.map { it.toString() }
            val key = stringTokens.joinToString("")
            val distMap = dist.samples.associate { it.first.toString() to it.second }

            key to MarkovChainSerializableEntry(key, stringTokens, distMap)
        }

    private val serialKeys =
        transitionDists.entries.associate { (key, _) -> key.joinToString("") { it.toString() } to key }

    private val levenshteinDistance = LevenshteinDistance.getDefaultInstance()

    private fun quickNearestKeys(sequence: List<T>): List<List<T>> {
        if (serialKeys.isEmpty()) {
            return emptyList()
        }

        val serialKey = sequence.takeLast(windowSize).joinToString("") { it.toString() }
        val nearestSerialKeys = serialKeys.keys.sortedBy {
            levenshteinDistance.apply(serialKey, it)
        }
        return nearestSerialKeys.map { serialKeys[it]!! }
    }

    private fun neighborhoodDistribution(sequence: List<T>): DiscreteDist<T>? {
        // Note nearest-K may include exact sequence distribution since we don't know that there's no match
        val nearestK = quickNearestKeys(sequence)
        val targetSampleSize = 8

        val combinedSample = mutableListOf<Pair<T, Int>>()
        var totalSampleSize = 0
        for (neighbor in nearestK) {
            val newSample = transitionDists[neighbor]?.samples ?: emptyList()
            totalSampleSize += newSample.sumOf { it.second }
            combinedSample.addAll(newSample)

            if (totalSampleSize >= targetSampleSize) {
                break
            }
        }

        val reducedCombinedSample = combinedSample
            .groupBy { it.first }
            .mapValues { (_, pairList) ->
                pairList.sumOf { it.second }
            }
            .toList()

        return if (reducedCombinedSample.isEmpty()) {
            null
        } else {
            DiscreteDist(reducedCombinedSample)
        }
    }

    fun sample(sequence: List<T>): T? {
        if (transitionDists.isEmpty()) {
            return null
        }

        if (sequence.size < windowSize) {
            return null
        }

        val window = sequence.takeLast(windowSize)
        val dist = neighborhoodDistribution(window)
            ?: return null

        return dist.sample(random)
    }

    /**
     * Sample predictions for eligible windows of the given sequences and return (successes, trials).
     */
    fun evaluateOverSequences(
        sequences: List<List<T>>,
    ): Pair<Int, Int> {
        var successes = 0
        var failures = 0

        sequences.forEach { sequence ->
            sequence.windowed(windowSize + 1, step = 1, partialWindows = false).forEach { window ->
                val input = window.dropLast(1)
                val expectedOutput = window.last()

                val sampledOutput = sample(input)
                if (sampledOutput == null || sampledOutput != expectedOutput) {
                    failures++
                } else {
                    successes++
                }
            }
        }

        return successes to (successes + failures)
    }

    fun sampleExtendByN(sequence: List<T>, n: Int): List<T> {
        var added = 0
        val extendedSequence = sequence.toMutableList()
        while (added < n) {
            val next = sample(extendedSequence) ?: break
            extendedSequence.add(next)
            added++
        }

        return extendedSequence
    }

    /**
     * Sample until condition is met, extending sequence and including the final element
     * which satisfies the condition.
     * Return null if inference fails at any point.
     */
    fun sampleUntil(sequence: List<T>, maxN: Int, condition: (T) -> Boolean): List<T>? {
        var added = 0
        val extendedSequence = sequence.toMutableList()
        while (added < maxN) {
            val next = sample(extendedSequence) ?: return null
            extendedSequence.add(next)

            if (condition(next)) {
                break
            }
            added++
        }

        return extendedSequence
    }

    fun extendTraining(
        sequences: List<List<T>>,
    ): MarkovChain<T> {
        val sampleMap = transitionDists.mapValues { (_, dist) ->
            dist.samples.toMap().toMutableMap()
        }.toMutableMap()

        return train(sequences, windowSize, sampleMap, mutableMapOf()) // Need initializer?
    }

    fun serializableForm(): MarkovChainSerializableForm {
        val stringTransitions = transitionDists
            .map { (typedTokens, dist) ->
                val stringTokens = typedTokens.map { it.toString() }
                val key = stringTokens.joinToString("")
                val distMap = dist.samples.associate { it.first.toString() to it.second }

                MarkovChainSerializableEntry(key, stringTokens, distMap)
            }

        return MarkovChainSerializableForm(
            transitionDistributions = stringTransitions,
            windowSize = windowSize
        )
    }

    fun writeTo(file: File) {
        val serializableForm = this.serializableForm()
        file.outputStream().let { os ->
            ObjectOutputStream(os).writeObject(serializableForm)
        }
    }

    companion object {

        fun fromSerializableForm(
            serializableForm: MarkovChainSerializableForm,
        ): MarkovChain<String> {
            val transitionDistributions = serializableForm.transitionDistributions
                .associate {
                    it.tokens to DiscreteDist(it.targetDistribution.toList())
                }
            return MarkovChain(
                transitionDistributions,
                serializableForm.windowSize,
            )
        }

        fun loadFrom(
            file: File
        ): MarkovChain<String> {
            val markovChainAny = ObjectInputStream(file.inputStream()).readObject()
            if (markovChainAny !is MarkovChain<*>) {
                throw InvalidObjectException("File at ${file.path} uninterpretable as ${this::class.java.simpleName}")
            }

            @Suppress("UNCHECKED_CAST")
            return markovChainAny as MarkovChain<String>
        }

        private val random = Random(281411122)

        private fun <T : Any> train(
            sequences: List<List<T>>,
            windowSize: Int,
            sampleMap: MutableMap<List<T>, MutableMap<T, Int>>,
            sampleInitializer: Map<T, Int>, // Optional initial value for samples to avoid overly sparse
        ): MarkovChain<T> {
            sequences.forEach { sequence ->
                sequence.windowed(windowSize + 1, step = 1, partialWindows = false).forEach { window ->
                    val input = window.dropLast(1)
                    val output = window.last()

                    sampleMap.compute(input) { _, m ->
                        (m ?: sampleInitializer.toMutableMap()).also {
                            it.compute(output) { _, v ->
                                (v ?: 0) + 1
                            }
                        }
                    }
                }
            }

            return MarkovChain(
                sampleMap.mapValues { DiscreteDist(it.value.toList()) },
                windowSize,
            )
        }

        fun <T : Any> train(
            sequences: List<List<T>>,
            windowSize: Int,
            sampleInitializer: Map<T, Int> = emptyMap(),
        ): MarkovChain<T> {
            return train(
                sequences,
                windowSize,
                mutableMapOf(),
                sampleInitializer = sampleInitializer,
            )
        }
    }
}