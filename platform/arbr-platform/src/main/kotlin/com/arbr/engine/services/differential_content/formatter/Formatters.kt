package com.arbr.engine.services.differential_content.formatter

import com.arbr.content_formats.format.tokenizer.TokenizationSerializer
import com.arbr.data_structures_common.partial_order.PartialOrder
import com.arbr.model_loader.indents.TokenFormatterCompiler

object Formatters {

    private fun <T> dfsIndexMap(partialOrder: PartialOrder<T>, f: (Int, T) -> T): PartialOrder<T> {
        val elementIndexMap = mutableMapOf<T, Int>()

        var index = 0
        partialOrder.dfs {
            if (it != null) {
                elementIndexMap[it] = index
                index++
            }
        }

        return partialOrder.map { elt ->
            val eltIndex = elementIndexMap[elt]!!
            f(eltIndex, elt)
        }
    }

    fun <DocumentType, TokenType> combine(
        baseSerializer: TokenizationSerializer<DocumentType, TokenType>,
        formatterCompilers: List<TokenFormatterCompiler<DocumentType, TokenType>>,
    ): TokenizationSerializer<DocumentType, TokenType> {
        return TokenizationSerializer { tokens, formatter ->
            val baseTokens = dfsIndexMap(tokens, formatter::formatToken)

            var serialization = baseSerializer.serialize(baseTokens)

            for (formatterCompiler in formatterCompilers) {
                val innerFormatter = formatterCompiler.compileFormatter(
                    baseTokens,
                    serialization,
                )
                serialization = baseSerializer.serializeWith(baseTokens, innerFormatter)
            }

            serialization
        }
    }

}