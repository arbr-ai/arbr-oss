package com.arbr.alignable.alignable.partial_order

import com.arbr.data_structures_common.partial_order.KeyedPartialOrderSet
import com.arbr.data_structures_common.partial_order.KeyedValue
import com.arbr.data_structures_common.partial_order.Orders
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonTypeRef
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Instant
import kotlin.test.assertEquals

class KeyedPartialOrderSetTest {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    private data class SegmentIndexReprTree(
        val contentType: String,
        val ruleName: String,
        val name: String?,
        val elementIndex: Int,
        val startIndex: Int,
        val endIndex: Int,
        val content: String,
        val childElements: List<SegmentIndexReprTree>,
    ): KeyedValue<String> {

        @JsonIgnore
        override val key: String = listOf(
                contentType,
                ruleName,
                elementIndex.toString(),
                startIndex.toString(),
                endIndex.toString(),
            ).joinToString("-")
    }

    @Test
    fun `behaves like a poset`() {
        val mapper = jacksonObjectMapper()
        val nodeListFile = File("src/test/resources/content/node_list.json")
        val elementList = mapper.readValue(nodeListFile, jacksonTypeRef<List<SegmentIndexReprTree>>())

        val relationListFile = File("src/test/resources/content/relation_list.json")
        val relationList = mapper.readValue(relationListFile, jacksonTypeRef<List<Pair<SegmentIndexReprTree, SegmentIndexReprTree>>>())

        val keyRelationMap: Map<String, MutableList<String>> = relationList
            .map { (tree0, tree1) ->
                tree0.key to tree1.key
            }
            .groupingBy { it.first }
            .aggregate { _, accumulator, element, _ ->
                if (accumulator == null) {
                    mutableListOf(element.second)
                } else {
                    accumulator
                        .add(element.second)
                    accumulator
                }
            }

        val keyedPoset = KeyedPartialOrderSet.ofChildMap(
            elementList.associateBy { it.key },
            keyRelationMap
        )
        val startMs = Instant.now().toEpochMilli()
        keyedPoset.dfs { }
        val endMs = Instant.now().toEpochMilli()
        println()
        println(endMs - startMs)
    }

    @Test
    fun `constructs from list`() {
        val poset = Orders.keyedListPoset(
            listOf("banana", "potato", "catdog"),
        )

        val minima = poset.minima()
        assertEquals(1, minima.size)
        assertEquals("banana", minima[0].element)

        val maxima = poset.maxima()
        assertEquals(1, maxima.size)
        assertEquals("catdog", maxima[0].element)
    }

    @Test
    fun `performs DFS in expected order`() {
        val intRange = 1..6

        // 1
        //  2
        //   3
        //   4
        //  5
        //  6
        val elementList = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15)
        val relationList = listOf(
            0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 4 to 5, 5 to 6, 5 to 10, 5 to 10, 6 to 7, 6 to 10,
            7 to 8, 8 to 9, 10 to 11, 10 to 11, 10 to 14, 10 to 14, 11 to 12, 12 to 13, 14 to 15
        ).distinct()

        val keyedElements = elementList.map {
            Orders.IndexKeyedValue(it, it)
        }

        val childMap = relationList.filter { it.first <= 15 && it.second <= 15 }
            .groupBy { it.first }
            .mapValues { (_, l) -> l.map { it.second } }
        val poset = KeyedPartialOrderSet.ofChildMap(
            keyedElements.associateBy { it.key },
            childMap,
        )

        var s = ""
        poset.dfsPreAndPostfix(
            {
                if (it != null) {
                    println("$s<$it>")
                    s += " "
                }
            },
            {
                if (it != null) {
                    s = s.drop(1)
                    println("$s</$it>")
                }
            }
        )
    }

}