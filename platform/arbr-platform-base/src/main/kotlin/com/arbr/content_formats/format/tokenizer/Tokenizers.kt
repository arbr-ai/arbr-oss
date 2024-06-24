package com.arbr.content_formats.format.tokenizer

import com.arbr.data_structures_common.partial_order.MapViewPartialOrder
import com.arbr.data_structures_common.partial_order.Orders
import com.arbr.data_structures_common.partial_order.PartialOrder

object Tokenizers {

    fun <D, S: PartialOrder<T>, T: Any, S1: PartialOrder<T1>, T1: Any> compose(
        outerTokenizer: Tokenizer<D, S, T>,
        innerTokenizer: Tokenizer<T, S1, T1>,
    ): Tokenizer<D, MapViewPartialOrder<T, T1>, T1> =
        Tokenizer { document ->
            val outerTokens = outerTokenizer.tokenize(document)

            Orders.mapView(outerTokens) { outerToken ->
                innerTokenizer.tokenize(outerToken)
            }
        }

}

