package com.arbr.alignable.alignable.partial_order

import com.arbr.data_structures_common.immutable.ImmutableLinkedSet
import com.arbr.data_structures_common.partial_order.ConcretePartialOrderSet
import com.arbr.data_structures_common.partial_order.posetOf
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PartialOrderSetTest {

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

        val poset = ConcretePartialOrderSet(
            elementList.filter { it <= 15 }.toSet(),
            relationList.filter { it.first <= 15 &&  it.second <= 15 },
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

    @Test
    fun `performs DFS efficiently`() {
        // Too slow because of deep equality+hash computations, hence the need for KeyedPartialOrderSet
//        val mapper = jacksonObjectMapper()
//        val nodeListFile = File("src/test/resources/content/node_list.json")
//        val elementList = mapper.readValue(nodeListFile, jacksonTypeRef<List<SegmentIndexReprTree>>())
//
//        val relationListFile = File("src/test/resources/content/relation_list.json")
//        val relationList = mapper.readValue(relationListFile, jacksonTypeRef<List<Pair<SegmentIndexReprTree, SegmentIndexReprTree>>>())
//
//        val poset = ConcretePartialOrderSet(ImmutableLinkedSet(elementList), relationList)
//        val startMs = Instant.now().toEpochMilli()
//        poset.dfs { }
//        val endMs = Instant.now().toEpochMilli()
//        println()
//        println(endMs - startMs)
    }

    @Test
    fun `performs DFS in viable order 1`() {
        // Simple linear order
        val intRange = 1..4
        val poset = posetOf<Int>(
            ImmutableLinkedSet(intRange.toList()),
            intRange.mapNotNull { i ->
                i to i - 1
            }
        )

        var indent = ""
        poset.dfsPreAndPostfix({ z ->
            println(indent + z.toString())
            indent += "  "

        }) { z ->
            indent = indent.drop(2)
            println(indent + z.toString())
        }
    }

    @Test
    fun `performs DFS in viable order 2`() {
        // Balanced binary tree order
        val intRange = 1..8
        val poset = posetOf<Int>(
            ImmutableLinkedSet(intRange.toList()),
            intRange.mapNotNull { i ->
                if (i == 1) {
                    null
                } else {
                    i / 2 to i
                }
            },
            elementSoftOrder = Comparator.naturalOrder(),
        )

        val executionOrder = mutableListOf<String>()
        var indent = ""
        poset.dfsPreAndPostfix({ z ->
            println(indent + "<${z ?: "root"}>")
            executionOrder.add("<${z ?: "root"}>")
            indent += "  "

        }) { z ->
            indent = indent.drop(2)
            println(indent + "</${z ?: "root"}>")
            executionOrder.add("</${z ?: "root"}>")
        }

        val expectedString = """
            <root>
              <1>
                <2>
                  <4>
                    <8>
                    </8>
                  </4>
                  <5>
                  </5>
                </2>
                <3>
                  <6>
                  </6>
                  <7>
                  </7>
                </3>
              </1>
            </root>
        """.trimIndent()
        assertEquals(
            expectedString.replace(Regex("\\s+"), ""),
            executionOrder.joinToString("").replace(Regex("\\s+"), ""),
        )
    }

    @Test
    fun `performs DFS in viable order on DAG`() {
        // DAG order
        val intRange = 1..6
        val poset = posetOf<Int>(
            ImmutableLinkedSet(intRange.toList()),
            listOf(
                1 to 2,
                1 to 3,
                2 to 4,
                3 to 4,
                3 to 5,
                4 to 6,
                5 to 6,
            ),
            elementSoftOrder = Comparator.naturalOrder(),
        )

        val executionOrder = mutableListOf<String>()
        var indent = ""
        poset.dfsPreAndPostfix({ z ->
            println(indent + "<${z ?: "root"}>")
            executionOrder.add("<${z ?: "root"}>")
            indent += "  "

        }) { z ->
            indent = indent.drop(2)
            println(indent + "</${z ?: "root"}>")
            executionOrder.add("</${z ?: "root"}>")
        }

        val expectedString = """
            <root>
              <1>
                <2>
                </2>
                <3>
                  <4>
                  </4>
                  <5>
                      <6>
                      </6>
                  </5>
                </3>
              </1>
            </root>
        """.trimIndent()
        assertEquals(
            expectedString.replace(Regex("\\s+"), ""),
            executionOrder.joinToString("").replace(Regex("\\s+"), ""),
        )
    }

    @Test
    fun `finds cycles`() {
        val poset = posetOf(
            ImmutableLinkedSet(listOf(0, 1, 2, 3)),
            listOf(
                0 to 1,
                1 to 2,
                2 to 0,
                3 to 0,
            )
        )

        val cycles = poset.findCycles()
        assertTrue(cycles.isNotEmpty())
    }

}