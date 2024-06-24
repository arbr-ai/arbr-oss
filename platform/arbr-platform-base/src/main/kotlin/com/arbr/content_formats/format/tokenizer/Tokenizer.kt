package com.arbr.content_formats.format.tokenizer

import com.arbr.data_structures_common.partial_order.LinearOrderList
import com.arbr.data_structures_common.partial_order.Orders
import com.arbr.data_structures_common.partial_order.PartialOrder
import com.arbr.data_structures_common.partial_order.PartialOrderSet

fun interface Tokenizer<DocumentType, Tokens : PartialOrder<TokenType>, TokenType> {

    fun tokenize(document: DocumentType): Tokens

    fun <D> mapFrom(e: (D) -> DocumentType) = this.let { parent ->
        Tokenizer<D, Tokens, TokenType> { d ->
            parent.tokenize(e(d))
        }
    }

    fun <U, Q : PartialOrder<U>> mapInto(tokenOrder: Q, f: (TokenType) -> U): Tokenizer<DocumentType, Q, U> =
        this.let { parent ->
            Tokenizer { document ->
                val outerTokens = parent.tokenize(document)
                Orders.mapInto(tokenOrder, outerTokens, f)
            }
        }

    fun <U, Q : PartialOrder<U>> mapIntoNotNull(tokenOrder: Q, f: (TokenType) -> U?): Tokenizer<DocumentType, Q, U> =
        this.let { parent ->
            Tokenizer { document ->
                val outerTokens = parent.tokenize(document)
                Orders.mapIntoNotNull(tokenOrder, outerTokens, f)
            }
        }
}

interface LinearTokenizer<DocumentType, TokenType> :
    Tokenizer<DocumentType, LinearOrderList<TokenType>, TokenType>

interface PosetTokenizer<DocumentType, TokenType> :
    Tokenizer<DocumentType, PartialOrderSet<TokenType>, TokenType>

interface LinearPlaintextTokenizer<TokenType> :
    LinearTokenizer<String, TokenType>

interface SimpleTextTokenizer : LinearPlaintextTokenizer<String>

interface IndexedTextTokenizer : LinearPlaintextTokenizer<Pair<Int, String>>
