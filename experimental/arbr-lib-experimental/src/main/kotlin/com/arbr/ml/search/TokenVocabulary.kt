package com.arbr.ml.search

class TokenVocabulary<T: Any>(
    document: List<T>,
    private val pair: (T, T) -> T
): ContractionEncoder<T> {
    private val encoder: MutableMap<T, Int> = mutableMapOf()
    private val decoder: MutableMap<Int, T> = mutableMapOf()

    /**
     * Build initial encoder and decoder maps based on the first index each symbol appears in the document.
     */
    init {
        document.withIndex().forEach { (code, symbol) ->
            if (symbol !in encoder) {
                encoder[symbol] = code
                decoder[code] = symbol
            }
        }
    }

    @Synchronized
    fun encodeAll(symbols: List<T>): List<Int> {
        return symbols.map { s ->
            val enc = encoder[s]
                ?: throw Exception("Missing encoding for symbol $s")
            enc.also { decoder[it] = s }
        }
    }

    @Synchronized
    fun decodeAll(codes: List<Int>): List<T> {
        return codes.map { c ->
            val dec = decoder[c]
                ?: throw Exception("Missing decoding for symbol $c")
            dec
        }
    }

    @Synchronized
    override fun encode(element: T): Int {
        return encodeAll(listOf(element))[0]
    }

    @Synchronized
    override fun decode(code: Int): T {
        return decodeAll(listOf(code))[0]
    }

    @Synchronized
    fun encodePair(symbol0: T, symbol1: T): Int {
        val combinedSymbol = pair(symbol0, symbol1)
        return encode(combinedSymbol)
    }

    override fun identityCode(): Int {
        return -1
    }

    @Synchronized
    override fun combine(leftCode: Int, rightCode: Int): Int {
        val codes = decodeAll(listOf(leftCode, rightCode))
        return encodePair(codes[0], codes[1])
    }
}