package com.arbr.ml.search

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class TokenizationDocumentModelTest {

    @Test
    fun initializes() {
        val t = listOf(0, 0, 1, 0, 1, 1, 0)
        val p = listOf(0, 1, 2, 0, 2, 1, 0)
        val q = listOf(1, 0, 1, 1, 0, 0, 2)

        val tks = (t.indices).map { i ->
            TaggedToken(
                t[i].toString(),
                p[i],
                q[i],
            )
        }

        val model = TokenizationDocumentModel.fromBaseDocument(
            tks
        )

//        assertEquals(1, model.tokenPositivity[0])
//        assertEquals(5, model.tokenPositivity[1])
//        assertEquals(4, model.tokenNegativity[0])
//        assertEquals(1, model.tokenNegativity[1])
        println(model)
    }

    @Test
    fun contracts() {
        val t = listOf(0, 0, 1, 0, 1, 1, 0)
        val p = listOf(0, 1, 2, 0, 2, 1, 0)
        val q = listOf(1, 0, 1, 1, 0, 0, 2)

        val tks = (t.indices).map { i ->
            TaggedToken(
                t[i].toString(),
                p[i],
                q[i],
            )
        }

        val model = TokenizationDocumentModel.fromBaseDocument(
            tks
        )

//        assertEquals(1, model.tokenPositivity[0])
//        assertEquals(5, model.tokenPositivity[1])
//        assertEquals(4, model.tokenNegativity[0])
//        assertEquals(1, model.tokenNegativity[1])

//        val model1 = run {
//            val contraction = TokenCodeContraction.pair(
//                0, 1
//            )
//            val contracted = model.contract(contraction)
//            println(contracted)
//
//            assertEquals(0, contracted.tokenPositivity[0])
//            assertEquals(1, contracted.tokenPositivity[1])
//            assertEquals(5, contracted.tokenPositivity[187])
//
//            assertEquals(3, contracted.tokenNegativity[0])
//            assertEquals(0, contracted.tokenNegativity[1])
//            assertEquals(2, contracted.tokenNegativity[187])
//
//            contracted
//        }
//
//        run {
//            val contraction = TokenCodeContraction.pair(
//                0, 1
//            )
//            val contracted = model1.contract(contraction)
//            println(contracted)
//
//            assertEquals(0, contracted.tokenPositivity[0])
//            assertEquals(1, contracted.tokenPositivity[1])
//            assertEquals(5, contracted.tokenPositivity[187])
//
//            assertEquals(3, contracted.tokenNegativity[0])
//            assertEquals(0, contracted.tokenNegativity[1])
//            assertEquals(2, contracted.tokenNegativity[187])
//        }
    }

}