package com.arbr.alignable.alignable.v2.dag//package com.arbr.alignable.alignable.v2.dag
//
//import com.arbr.platform.alignable.alignable.Alignable
//import com.arbr.platform.alignable.alignable.alignment.Alignment
//import com.arbr.platform.alignable.alignable.collections.SequenceAlignmentOperation
//import com.arbr.platform.alignable.alignable.text.SimpleAlignableString
//import com.arbr.platform.alignable.alignable.text.SimpleStringAlignmentOperation
//import org.junit.jupiter.api.Test
//import java.util.*
//import kotlin.jvm.optionals.getOrNull
//import kotlin.math.abs
//import kotlin.math.max
//
//class AdjacencyMatrixDAGAlignmentHelperTest {
//
//
//    private fun bitVector(z: Int): List<Int> {
//        var l = z
//        val bits = mutableListOf<Int>()
//        while (l > 0) {
//            bits.add(l % 2)
//            l /= 2
//        }
//        return bits
//    }
//
//    private fun stateString(state: AdjacencyMatrixDAGAlignmentTraversalState<*, *, *>): String {
//        val targetPath = bitVector(state.targetState).joinToString(", ") { it.toString() }
//        val sourcePath = bitVector(state.sourceState).joinToString(", ") { it.toString() }
//        return "TS[\n\tt=<$targetPath>,\n\ts=<$sourcePath>,\n\tq=<${state.constructionState}>\n${state.latestOperation}\n]"
//    }
//
//    private fun matrixFromAdjacencyList(n: Int, adjacencyList: Map<Int, List<Int>>): List<List<Boolean>> {
//        return (0 until n).map { i ->
//            val row = adjacencyList[i] ?: emptyList()
//            (0 until n).map { j ->
//                j in row
//            }
//        }
//    }
//
//    private val stringLengthInducer =
//        object : ConstructiveDAGStateInducer<Int, SimpleAlignableString, SimpleStringAlignmentOperation> {
//            override val initialState: Int = -1
//            override val initialStateCode: Int = -1
//
//            override fun apply(
//                state: Int,
//                stateCode: Int,
//                operation: AlignableDAGNode<SimpleAlignableString, SimpleStringAlignmentOperation, SimpleAlignableString, SimpleStringAlignmentOperation>
//            ): Pair<Int, Int> {
//                val v = state + operation.node.text.length
//                return v to v
//            }
//
//            override fun viableOperations(
//                fromState: Int,
//                targetOperation: Optional<AlignableDAGNode<SimpleAlignableString, SimpleStringAlignmentOperation, SimpleAlignableString, SimpleStringAlignmentOperation>>,
//                sourceOperation: Optional<AlignableDAGNode<SimpleAlignableString, SimpleStringAlignmentOperation, SimpleAlignableString, SimpleStringAlignmentOperation>>
//            ): List<Alignment<AlignableDAGNode<SimpleAlignableString, SimpleStringAlignmentOperation, SimpleAlignableString, SimpleStringAlignmentOperation>, Pair<SimpleStringAlignmentOperation?, SequenceAlignmentOperation<NormativeEdge<SimpleAlignableString, SimpleStringAlignmentOperation>, NormativeEdgeAlignmentOperation<SimpleAlignableString, SimpleStringAlignmentOperation>>?>>> {
//                return if (targetOperation.isPresent && sourceOperation.isPresent) {
//                    listOf(
//                        targetOperation.get().align(sourceOperation.get())
//                    )
//                } else if (targetOperation.isPresent) {
//                    listOf(Alignment.Equal(targetOperation.get(), targetOperation.get()))
//                } else if (sourceOperation.isPresent) {
//                    listOf(Alignment.Equal(sourceOperation.get(), sourceOperation.get()))
//                } else {
//                    emptyList()
//                }
//            }
//        }
//
//    @Test
//    fun `computes shortest path`() {
//        val targetAdjacencyMatrix = listOf(
//            listOf(false, true, true, false),
//            listOf(false, false, false, true),
//            listOf(false, false, false, true),
//            listOf(false, false, false, false),
//        )
//        val targetValues = listOf("aa", "ab", "cc", "cd").map { SimpleAlignableString(it) }
//
//        val sourceAdjacencyMatrix = listOf(
//            listOf(false, true, true, false),
//            listOf(false, false, false, true),
//            listOf(false, false, false, true),
//            listOf(false, false, false, false),
//        )
//        val sourceValues = listOf("aa", "ba", "cc", "dc").map { SimpleAlignableString(it) }
//
//        val (path, cost) = AdjacencyMatrixDAGAlignmentHelper(
//            AdjacencyMatrixDAGValued(targetAdjacencyMatrix, targetValues),
//            AdjacencyMatrixDAGValued(sourceAdjacencyMatrix, sourceValues),
//            stringLengthInducer,
//            AdjacencyMatrixDAGAlignmentConfiguration(
//                0.1,
//                1.0,
//                1.5,
//                10000.0,
//                0.01,
//            ),
//        ).alignmentPath()!!
//
//        println(path.map { stateString(it) })
//        println(cost)
//    }
//
//    @Test
//    fun `computes shortest path with non-trivial viability`() {
//        val miniLanguageInducer =
//            object :
//                ConstructiveDAGStateInducer<List<Pair<List<String>, String>>, SimpleAlignableString, SimpleStringAlignmentOperation> {
//                // AUG -> [GUA, ACT]
//                // GUA -> [ACT, TCT]
//                // *CT -> [AUG, GUA]
//                override val initialState: List<Pair<List<String>, String>> = emptyList()
//                override val initialStateCode: Int = 3
//                override fun apply(
//                    state: List<Pair<List<String>, String>>,
//                    stateCode: Int,
//                    operation: AlignableDAGNode<SimpleAlignableString, SimpleStringAlignmentOperation, SimpleAlignableString, SimpleStringAlignmentOperation>
//                ): Pair<List<Pair<List<String>, String>>, Int> {
//                    val text = operation.node.text.take(3)
////                    val idxPair = state.findLastAnyOf(parentOperations.map { it.text })
////
////                    val nextText = if (idxPair != null) {
////                        state.substring(0, idxPair.first + 3) + "[$text]" + state.substring(idxPair.first + 3)
////                    } else {
////                        state + text
////                    }
//
//                    val parents = operation.parentEdges.elements.mapNotNull { it.label.getOrNull()?.text }
//                    val nextState = state + listOf(
//                        parents to text
//                    )
//
//                    val nextStateCode =
//                        stateCode * 31 + (text + "<" + parents.joinToString(",") { it }).hashCode()
//                    return nextState to nextStateCode
//                }
//
//                private fun viableNextTokens(state: List<Pair<List<String>, String>>): List<String> {
//                    val sstate = state.lastOrNull()?.second ?: "AUG"
//                    return when {
//                        sstate.endsWith("AUG") -> listOf("GUA", "ACT")
//                        sstate.endsWith("GUA") -> listOf("ACT", "TCT")
//                        sstate.endsWith("CT") -> listOf("AUG", "GUA")
//                        else -> emptyList()
//                    }
//                }
//
//                override fun viableOperations(
//                    fromState: List<Pair<List<String>, String>>,
//                    targetOperation: Optional<AlignableDAGNode<SimpleAlignableString, SimpleStringAlignmentOperation, SimpleAlignableString, SimpleStringAlignmentOperation>>,
//                    sourceOperation: Optional<AlignableDAGNode<SimpleAlignableString, SimpleStringAlignmentOperation, SimpleAlignableString, SimpleStringAlignmentOperation>>
//                ): List<Alignment<AlignableDAGNode<SimpleAlignableString, SimpleStringAlignmentOperation, SimpleAlignableString, SimpleStringAlignmentOperation>, Pair<SimpleStringAlignmentOperation?, SequenceAlignmentOperation<NormativeEdge<SimpleAlignableString, SimpleStringAlignmentOperation>, NormativeEdgeAlignmentOperation<SimpleAlignableString, SimpleStringAlignmentOperation>>?>>> {
//                    val viable = viableNextTokens(fromState)
//                        .map {
//                            SimpleAlignableString(it)
//                        }
//                    return if (targetOperation.isPresent && sourceOperation.isPresent) {
//                        viable
//                            .filter { v ->
//                                val vn = AlignableDAGNode(
//                                    v,
//                                    sourceOperation.get().parentEdges,
//                                )
//                                sourceOperation.get().align(vn).cost < 0.3
//                            }
//                            .map { v ->
//                                val vn = AlignableDAGNode(
//                                    v,
//                                    targetOperation.get().parentEdges,
//                                )
//                                targetOperation.get().align(vn)
//                            }
//                    } else if (targetOperation.isPresent) {
//                        viable
//                            .map { v ->
//                                val vn = AlignableDAGNode(
//                                    v,
//                                    targetOperation.get().parentEdges,
//                                )
//                                targetOperation.get().align(vn)
//                            }
//                    } else if (sourceOperation.isPresent) {
//                        viable
//                            .map { v ->
//                                val vn = AlignableDAGNode(
//                                    v,
//                                    sourceOperation.get().parentEdges,
//                                )
//                                sourceOperation.get().align(vn)
//                            }
//                    } else {
//                        emptyList()
//                    }.filter { it.cost <= 1.0 }
//                }
//
//            }
//
//        val adjacencyMatrix = matrixFromAdjacencyList(
//            4, mapOf(
//                0 to listOf(1, 2),
//                1 to listOf(2, 3),
//                2 to listOf(3),
//                3 to listOf(),
//            )
//        )
//        // AUG -> [GUA, ACT]
//        // GUA -> [ACT, TCT]
//        // *CT -> [AUG, GUA]
//        val targetValues = listOf("GUA", "ACT", "GUA", "ACT").map { SimpleAlignableString(it) }
//        val sourceValues = listOf("GUA", "TCT", "AUG", "ACT").map { SimpleAlignableString(it) }
//
//        val (path, cost) = AdjacencyMatrixDAGAlignmentHelper(
//            AdjacencyMatrixDAGValued(adjacencyMatrix, targetValues),
//            AdjacencyMatrixDAGValued(adjacencyMatrix, sourceValues),
//            miniLanguageInducer,
//            AdjacencyMatrixDAGAlignmentConfiguration(
//                0.1,
//                1.0,
//                1.5,
//                10000.0,
//                0.01,
//            ),
//        ).alignmentPath()!!
//
//        println(path.joinToString("\n") { stateString(it) })
//        println(cost)
//    }
//
//    data class Node(val x: Int) : Alignable<Node, Int> {
//        override fun applyAlignment(alignmentOperations: List<Int>): Node {
//            return Node(x + alignmentOperations.sum())
//        }
//
//        override fun align(e: Node): Alignment<Node, Int> {
//            return if (x == e.x) {
//                Alignment.Equal(this, e)
//            } else {
//                Alignment.Align(
//                    listOf(e.x - x),
//                    (e.x - x) * 1.0,
//                    this,
//                    e,
//                )
//            }
//        }
//
//        override fun empty(): Node {
//            return Node(0)
//        }
//    }
//
//    @Test
//    fun `computes shortest path with even simpler viability`() {
//        val miniLanguageInducer =
//            object : ConstructiveDAGStateInducer<List<Pair<List<Node>, Node>>, Node, Int> {
//
//                override val initialState: List<Pair<List<Node>, Node>>
//                    get() = emptyList()
//                override val initialStateCode: Int
//                    get() = 0
//
//                override fun apply(
//                    state: List<Pair<List<Node>, Node>>,
//                    stateCode: Int,
//                    operation: AlignableDAGNode<Node, Int, Node, Int>
//                ): Pair<List<Pair<List<Node>, Node>>, Int> {
//                    val parents = operation.parentEdges.mapNotNull { it.label.getOrNull() }
//                    return (state + listOf(parents to operation.node)) to (stateCode + 31 * operation.node.x + 7 + parents.sumOf { it.x * 3 })
//                }
//
//                override fun viableOperations(
//                    fromState: List<Pair<List<Node>, Node>>,
//                    targetOperation: Optional<AlignableDAGNode<Node, Int, Node, Int>>,
//                    sourceOperation: Optional<AlignableDAGNode<Node, Int, Node, Int>>
//                ): List<Alignment<AlignableDAGNode<Node, Int, Node, Int>, Pair<Int?, SequenceAlignmentOperation<NormativeEdge<Node, Int>, NormativeEdgeAlignmentOperation<Node, Int>>?>>> {
//                    val viable = ((fromState.maxOfOrNull { it.second.x + 1 } ?: 0) until 11)
//                        .map {
//                            Node(it)
//                        }
//                    return if (targetOperation.isPresent && sourceOperation.isPresent) {
//                        viable
//                            .filter { v ->
//                                val vn = AlignableDAGNode(
//                                    v,
//                                    sourceOperation.get().parentEdges,
//                                )
//                                sourceOperation.get().align(vn).cost < 99.9
//                            }
//                            .map { v ->
//                                val vn = AlignableDAGNode(
//                                    v,
//                                    targetOperation.get().parentEdges,
//                                )
//                                targetOperation.get().align(vn)
//                            }
//                    } else if (targetOperation.isPresent) {
//                        viable
//                            .map { v ->
//                                val vn = AlignableDAGNode(
//                                    v,
//                                    targetOperation.get().parentEdges,
//                                )
//                                targetOperation.get().align(vn)
//                            }
//                    } else if (sourceOperation.isPresent) {
//                        viable
//                            .map { v ->
//                                val vn = AlignableDAGNode(
//                                    v,
//                                    sourceOperation.get().parentEdges,
//                                )
//                                sourceOperation.get().align(vn)
//                            }
//                    } else {
//                        emptyList()
//                    }.filter { it.cost <= 99.9 }
//                }
//            }
//
//        val targetAdjacencyMatrix = matrixFromAdjacencyList(
//            4, mapOf(
//                0 to listOf(1, 2),
//                1 to listOf(2, 3),
//                2 to listOf(3),
//                3 to listOf(),
//            )
//        )
//        val targetValues = listOf(0, 1, 2, 3).map { Node(it) }
//
//        val sourceAdjacencyMatrix = matrixFromAdjacencyList(
//            3, mapOf(
//                0 to listOf(1, 2),
//                1 to listOf(2),
//                2 to listOf(),
//            )
//        )
//        val sourceValues = listOf(0, 1, 3).map { Node(it) }
//
//        val (path, cost) = AdjacencyMatrixDAGAlignmentHelper(
//            AdjacencyMatrixDAGValued(targetAdjacencyMatrix, targetValues),
//            AdjacencyMatrixDAGValued(sourceAdjacencyMatrix, sourceValues),
//            miniLanguageInducer,
//            AdjacencyMatrixDAGAlignmentConfiguration(
//                0.1,
//                1.0,
//                1.5,
//                10000.0,
//                0.01,
//            ),
//        ).alignmentPath()!!
//
//        println(path.joinToString("\n") {
//            stateString(it)
//        })
//        println(cost)
//
//    }
//
//    data class DivNode(val x: Int) : Alignable<DivNode, Int> {
//        override fun applyAlignment(alignmentOperations: List<Int>): DivNode {
//            return DivNode(x + alignmentOperations.sum())
//        }
//
//        override fun align(e: DivNode): Alignment<DivNode, Int> {
//            return if (x == e.x) {
//                Alignment.Equal(this, e)
//            } else {
//                val z = if (x == 1 || e.x == 1) {
//                    max(e.x, x)
//                } else {
//                    (e.x % x) + abs(e.x - x) + 1
//                }
//                Alignment.Align(
//                    listOf(z),
//                    z * 1.0,
//                    this,
//                    e,
//                )
//            }
//        }
//
//        override fun empty(): DivNode {
//            return DivNode(1)
//        }
//    }
//
//    @Test
//    fun `computes shortest path with integer divisibility lattice`() {
//        val miniLanguageInducer =
//            object : ConstructiveDAGStateInducer<List<Pair<List<DivNode>, DivNode>>, DivNode, Int> {
//
//                override val initialState: List<Pair<List<DivNode>, DivNode>>
//                    get() = listOf(emptyList<DivNode>() to DivNode(1))
//                override val initialStateCode: Int
//                    get() = 1
//
//                override fun apply(
//                    state: List<Pair<List<DivNode>, DivNode>>,
//                    stateCode: Int,
//                    operation: AlignableDAGNode<DivNode, Int, DivNode, Int>
//                ): Pair<List<Pair<List<DivNode>, DivNode>>, Int> {
//                    val parents = operation.parentEdges.mapNotNull { it.label.getOrNull() }
//                    return (state + listOf(parents to operation.node)) to (stateCode + 31 * operation.node.x + 7 + parents.sumOf { it.x * 3 })
//                }
//
//                override fun viableOperations(
//                    fromState: List<Pair<List<DivNode>, DivNode>>,
//                    targetOperation: Optional<AlignableDAGNode<DivNode, Int, DivNode, Int>>,
//                    sourceOperation: Optional<AlignableDAGNode<DivNode, Int, DivNode, Int>>
//                ): List<Alignment<AlignableDAGNode<DivNode, Int, DivNode, Int>, Pair<Int?, SequenceAlignmentOperation<NormativeEdge<DivNode, Int>, NormativeEdgeAlignmentOperation<DivNode, Int>>?>>> {
//                    val factors = 2 until 5
//
////                    val viable = fromState.flatMap {
////                        factors.map { y ->
////                            DivNode(it.second.x * y)
////                        }
////                    }
//                    val viable = (2 until 100).map { DivNode(it) }
//                    return if (targetOperation.isPresent && sourceOperation.isPresent) {
//                        viable
//                            .map { v ->
//                                val vs = AlignableDAGNode(
//                                    v,
//                                    sourceOperation.get().parentEdges,
//                                )
//                                val sourceAlignment = sourceOperation.get().align(vs)
//
//                                val vn = AlignableDAGNode(
//                                    v,
//                                    targetOperation.get().parentEdges,
//                                )
//                                val targetAlignment = targetOperation.get().align(vn)
//
//                                Alignment.Align(
//                                    targetAlignment.operations,
//                                    targetAlignment.cost + sourceAlignment.cost,
//                                    targetAlignment.sourceElement,
//                                    targetAlignment.targetElement,
//                                )
//                            }
//                    } else if (targetOperation.isPresent) {
//                        viable
//                            .map { v ->
//                                val vn = AlignableDAGNode(
//                                    v,
//                                    targetOperation.get().parentEdges,
//                                )
//                                targetOperation.get().align(vn)
//                            }
//                    } else if (sourceOperation.isPresent) {
//                        viable
//                            .map { v ->
//                                val vn = AlignableDAGNode(
//                                    v,
//                                    sourceOperation.get().parentEdges,
//                                )
//                                sourceOperation.get().align(vn)
//                            }
//                    } else {
//                        emptyList()
//                    }.filter { it.cost <= 99.9 }
//                }
//            }
//
//        val targetAdjacencyMatrix = matrixFromAdjacencyList(
//            4, mapOf(
//                0 to listOf(1, 2, 3),
//                1 to listOf(2, 3),
//                2 to listOf(3),
//                3 to listOf(),
//            )
//        )
//        val targetValues = listOf(2, 3, 6, 4).map { DivNode(it) }
//
//        val sourceAdjacencyMatrix = matrixFromAdjacencyList(
//            4, mapOf(
//                0 to listOf(1, 2, 3),
//                1 to listOf(2, 3),
//                2 to listOf(3),
//                3 to listOf(),
//            )
//        )
//        val sourceValues = listOf(2, 3, 5, 4).map { DivNode(it) }
//
//        val (path, cost) = AdjacencyMatrixDAGAlignmentHelper(
//            AdjacencyMatrixDAGValued(targetAdjacencyMatrix, targetValues),
//            AdjacencyMatrixDAGValued(sourceAdjacencyMatrix, sourceValues),
//            miniLanguageInducer,
//            AdjacencyMatrixDAGAlignmentConfiguration(
//                0.1,
//                1.0,
//                1.5,
//                0.01,
//                0.01,
//            ),
//        ).alignmentPath()!!
//
//        println(path.joinToString("\n") {
//            stateString(it)
//        })
//        println(cost)
//
//    }
//
//}