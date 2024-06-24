package com.arbr.content_formats.tokens

import com.knuddels.jtokkit.Encodings
import com.knuddels.jtokkit.api.Encoding
import com.knuddels.jtokkit.api.EncodingRegistry
import com.knuddels.jtokkit.api.EncodingType


/**
 * CL_100k tokenization
 */
object TokenizationUtils {

    private val registry: EncodingRegistry = Encodings.newDefaultEncodingRegistry()
    private val enc: Encoding = registry.getEncoding(EncodingType.CL100K_BASE)

    fun getTokenCount(str: String): Int = enc.countTokens(str)

    fun tokenize(str: String): List<String> = enc.encode(str).map { enc.decode(listOf(it)) }

}
