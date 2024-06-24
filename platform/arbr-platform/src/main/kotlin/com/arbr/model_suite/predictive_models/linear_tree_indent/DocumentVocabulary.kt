package com.arbr.model_suite.predictive_models.linear_tree_indent

data class DocumentVocabulary(
    private val vocabularyList: List<String>,
    private val reservedIndexCount: Int,
) {
    val reservedIndices = (0 until reservedIndexCount).toList()
    private val vocabularyMap: Map<String, Int> = vocabularyList.withIndex().associate { it.value to reservedIndexCount + it.index }
    val size = reservedIndexCount + vocabularyList.size

    fun encode(word: String): Int {
        return vocabularyMap[word]
            ?: throw NoSuchElementException("No vocabulary entry for $word")
    }

    fun encodeOrNull(word: String): Int? {
        return vocabularyMap[word]
    }

    fun decode(wordIndex: Int): String {
        return vocabularyList[wordIndex - reservedIndexCount]
    }

    fun makeWordCountVector(wordIndexes: List<Int>): DoubleArray {
        val ancestorCounts = wordIndexes.groupingBy { it }.eachCount()

        return DoubleArray(size) { i ->
            ancestorCounts[i]?.toDouble() ?: 0.0
        }
    }

    fun makeWordVector(ancestorIndexes: List<Int>): DoubleArray {
        val ancestorCounts = ancestorIndexes.groupingBy { it }.eachCount()

        return DoubleArray(size) { i ->
            ancestorCounts[i]?.toDouble() ?: 0.0
        }
    }
}
