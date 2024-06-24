package com.arbr.tokenization.serializers

import com.arbr.content_formats.format.tokenizer.LineTokenizer
import com.arbr.content_formats.format.tokenizer.PatternSplitTextTokenizer
import com.arbr.content_formats.format.tokenizer.TokenizationSerializer
import com.arbr.content_formats.format.tokenizer.Tokenizers
import com.arbr.data_structures_common.partial_order.PartialOrderFlatteningScheme
import com.arbr.data_structures_common.partial_order.emptyPoset
import org.junit.jupiter.api.Test

class TokenizationSerializersTest {

    data class Document(
        val text: String
    )

    data class Line(
        val text: String
    )

    data class Token(
        val text: String
    )

    private val document0 = Document(
        """
            four score and seven years ago
            our founding fathers
        """.trimIndent()
    )

    @Test
    fun composes() {
        val outerTokenizer = LineTokenizer()
            .mapFrom<Document> { it.text }
            .mapInto(emptyPoset<Line>()) { Line(it.second) }
        val innerTokenizer = PatternSplitTextTokenizer("a")
            .mapFrom<Line> { it.text }
            .mapInto(emptyPoset()) { Token(it.second) }
        val composed = Tokenizers.compose(
            outerTokenizer,
            innerTokenizer
        )

        val tokens = composed.tokenize(document0)

        // [four score , nd seven ye, rs , go, our founding f, thers]
        println(tokens.toFlatList(scheme = PartialOrderFlatteningScheme.DEPTH_FIRST))

        // [four score , our founding f, nd seven ye, thers, rs , go]
        println(tokens.toFlatList(scheme = PartialOrderFlatteningScheme.BREADTH_FIRST))

        val outerSerializer = TokenizationSerializer<Document, Line> { po, _ ->
            Document(
                po.toFlatList(PartialOrderFlatteningScheme.DEPTH_FIRST).joinToString("\n==\n") { line ->
                    line.text
                }
            )
        }
        val innerSerializer = TokenizationSerializer<Line, Token> { po, _ ->
            Line(
                po.toFlatList(PartialOrderFlatteningScheme.DEPTH_FIRST).joinToString("|") { token ->
                    token.text
                }
            )
        }

        val flattener = TokenizationSerializers.flattener(
            outerSerializer,
            innerSerializer,
        ) { existingFamilies, _ ->
            val (ex, _) = existingFamilies.first()
            ex
        }

        /*
        Document(text=four score |nd seven ye|rs |go
        ==
        our founding f|thers)
         */
        val recoveredDocument0 = flattener.serialize(tokens.pairedView())
        println(recoveredDocument0)

        // Inject a token between two others
        // Both relation entries are required for guaranteed dfs order
        val newToken = Token("boards ")
        val afterToken = Token("four score ")
        val beforeToken = Token("nd seven ye")

        // TODO
        val recoveredDocument1 = flattener.serialize(
            tokens
                .push(newToken)
                .pushRelationship(afterToken, newToken)
                .pushRelationship(newToken, beforeToken)
                .pairedView()
        )
        println()

        /*
        Document(text=four score |boards |nd seven ye|rs |go
        ==
        our founding f|thers)
         */
        println(recoveredDocument1)
    }

    @Test
    fun `adds new unmapping family`() {
        val outerTokenizer = LineTokenizer()
            .mapFrom<Document> { it.text }
            .mapInto(emptyPoset<Line>()) { Line(it.second) }
        val innerTokenizer = PatternSplitTextTokenizer("a")
            .mapFrom<Line> { it.text }
            .mapInto(emptyPoset()) { Token(it.second) }
        val composed = Tokenizers.compose(
            outerTokenizer,
            innerTokenizer
        )

        val tokens = composed.tokenize(document0)

        // [four score , nd seven ye, rs , go, our founding f, thers]
        println(tokens.toFlatList(scheme = PartialOrderFlatteningScheme.DEPTH_FIRST))

        // [four score , our founding f, nd seven ye, thers, rs , go]
        println(tokens.toFlatList(scheme = PartialOrderFlatteningScheme.BREADTH_FIRST))

        val outerSerializer = TokenizationSerializer<Document, Line> { po, _ ->
            Document(
                po.toFlatList(PartialOrderFlatteningScheme.DEPTH_FIRST).joinToString("\n==\n") { line ->
                    line.text
                }
            )
        }
        val innerSerializer = TokenizationSerializer<Line, Token> { po, _ ->
            Line(
                po.toFlatList(PartialOrderFlatteningScheme.DEPTH_FIRST).joinToString("|") { token ->
                    token.text
                }
            )
        }

        val flattener = TokenizationSerializers.flattener(
            outerSerializer,
            innerSerializer,
        ) { _, newToken ->
            // Line("[${newToken.text}]")
            println("Lookup token: ${newToken.text}")
            null
        }

        /*
        Document(text=four score |nd seven ye|rs |go
        ==
        our founding f|thers)
         */
//        val recoveredDocument0 = flattener.serialize(tokens.pairedView())
//        println(recoveredDocument0)

        // Inject a token between two others
        // Both relation entries are required for guaranteed dfs order
        val newToken = Token("boards ")
        val afterToken = Token("four score ")
        val beforeToken = Token("nd seven ye")

        // TODO
        val recoveredDocument1 = flattener.serialize(
            tokens
                .push(newToken)
                .pushRelationship(afterToken, newToken)
                .pushRelationship(newToken, beforeToken)
                .pairedView()
        )
        println()

        /*
        Document(text=four score |nd seven ye|rs |go
        ==
        boards
        ==
        our founding f|thers)
         */
        println(recoveredDocument1)
    }
}
