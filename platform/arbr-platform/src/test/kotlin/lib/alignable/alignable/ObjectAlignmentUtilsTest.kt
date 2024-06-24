package com.arbr.alignable.alignable

//import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
//import com.arbr.relational_prompting.alignable.*
//import org.junit.jupiter.api.Assertions
//import org.junit.jupiter.api.Test
//
//private typealias ET = AlignableList<AtomicAlignable<Map.Entry<String, Any?>>, Map.Entry<String, Any?>>
//private typealias OT = SequenceAlignmentOperation<AtomicAlignable<Map.Entry<String, Any?>>, Map.Entry<String, Any?>>
//private typealias CT = AlignableList<AlignableHomogenousRootedDAG<ET, OT>, HomogenousRootedDAGAlignmentOperation<ET, OT>>
//
//private inline fun <reified E : Alignable<E, O>, reified O> seqListener(): SequenceAlignmentListener<E, O> {
//    return object : SequenceAlignmentListener<E, O>() {
//        override fun didAddElement(operationIndex: Int, targetElementIndex: Int, element: E) {
//            println("$operationIndex Add $targetElementIndex $element")
//        }
//
//        override fun didEditElement(operationIndex: Int, targetElementIndex: Int, fromElement: E, sequenceElement: E) {
//            println("$operationIndex Edit $targetElementIndex $sequenceElement")
//        }
//
//        override fun didRemoveElement(operationIndex: Int, targetElementIndex: Int, element: E) {
//            println("$operationIndex Delete $targetElementIndex $element")
//        }
//    }
//}
//
//private fun hrdagListener(): HomogenousRootedDAGAlignmentListener<ET, OT> {
//    return object : HomogenousRootedDAGAlignmentListener<ET, OT>() {
//        override fun didApplyNodeOperation(operationIndex: Int, operation: OT, element: ET) {
//            println("Node op")
//        }
//
//        override fun didDeleteChildType(
//            operationIndex: Int,
//            sequenceElement: AlignableList<AlignableHomogenousRootedDAG<ET, OT>, HomogenousRootedDAGAlignmentOperation<ET, OT>>
//        ) {
//            println("Del ct")
//        }
//
//        override fun didEditChildType(
//            operationIndex: Int,
//            targetElementIndex: Int,
//            fromElement: AlignableList<AlignableHomogenousRootedDAG<ET, OT>, HomogenousRootedDAGAlignmentOperation<ET, OT>>,
//            sequenceElement: AlignableList<AlignableHomogenousRootedDAG<ET, OT>, HomogenousRootedDAGAlignmentOperation<ET, OT>>
//        ) {
//            println("Edit ct")
//        }
//
//        override fun didInsertChildType(
//            operationIndex: Int,
//            sequenceElement: AlignableList<AlignableHomogenousRootedDAG<ET, OT>, HomogenousRootedDAGAlignmentOperation<ET, OT>>
//        ) {
//            println("Ins ct")
//        }
//
//        override fun didEnter(tree: AlignableHomogenousRootedDAG<ET, OT>) {
//            println("Enter sg")
//        }
//
//        override fun didAddSubgraph(subtree: AlignableHomogenousRootedDAG<ET, OT>, atIndex: Int) {
//            println("Add sg")
//        }
//
//        override fun didRemoveSubgraph(subtree: AlignableHomogenousRootedDAG<ET, OT>, atIndex: Int) {
//            println("Rem sg")
//        }
//
//        override fun didExit(tree: AlignableHomogenousRootedDAG<ET, OT>) {
//            println("Exit sg")
//        }
//    }
//}
//
//private fun alignableLinkedMap(
//    linkedMap: LinkedHashMap<String, out Any?>,
//    listener: SequenceAlignmentListener<AtomicAlignable<Map.Entry<String, Any?>>, Map.Entry<String, Any?>>,
//): AlignableList<AtomicAlignable<Map.Entry<String, Any?>>, Map.Entry<String, Any?>> = AlignableList(
//    linkedMap.map { AtomicAlignable(it) },
//    listener,
//)
//
//private fun alignableObj(
//    hrDag: HomogenousRootedDAG<out LinkedHashMap<String, out Any?>>,
//    listener: HomogenousRootedDAGAlignmentListener<ET, OT>,
//): AlignableHomogenousRootedDAG<ET, OT> {
//    return AlignableHomogenousRootedDAG<ET, OT>(
//        alignableLinkedMap(hrDag.nodeValue, seqListener()),
//        AlignableFrozenList(hrDag.children.map { cr ->
//            CT(cr.map { e ->
//                alignableObj(e, listener)
//            }, listener.subgraphListenerProxy)
//        }),
//        listener,
//    )
//}
//
//class ObjectAlignmentUtilsTest {
//
//    @Test
//    fun `aligns graphs`() {
//        val hrDag0 = ConcreteHomogenousRootedDAG(
//            linkedMapOf(
//                "name" to "Bob",
//                "job" to "hobnob",
//            ),
//            children = listOf(
//                listOf(
//                    ConcreteHomogenousRootedDAG(
//                        linkedMapOf(
//                            "name" to "Jake",
//                            "job" to "rake",
//                            "weakness" to "electricity",
//                        ),
//                        children = listOf()
//                    ),
//                    ConcreteHomogenousRootedDAG(
//                        linkedMapOf(
//                            "name" to "Steve",
//                            "job" to "believe",
//                        ),
//                        children = listOf()
//                    ),
//                ),
//                listOf(
//                    ConcreteHomogenousRootedDAG(
//                        linkedMapOf(
//                            "name" to "Alice",
//                            "vessel" to "chalice",
//                        ),
//                        children = listOf()
//                    ),
//                ),
//            )
//        )
//
//        val hrDag1 = ConcreteHomogenousRootedDAG(
//            linkedMapOf(
//                "name" to "Bob",
//                "job" to "corncob",
//            ),
//            children = listOf(
//                listOf(
//                    ConcreteHomogenousRootedDAG(
//                        linkedMapOf(
//                            "name" to "Jake",
//                            "job" to "rake",
//                            "weakness" to "electricity",
//                        ),
//                        children = listOf()
//                    ),
//                ),
//                listOf(
//                    ConcreteHomogenousRootedDAG(
//                        linkedMapOf(
//                            "name" to "Alice",
//                            "vessel" to "chalice",
//                            "disposition" to "malice",
//                        ),
//                        children = listOf()
//                    ),
//                ),
//            )
//        )
//
//        val e1 = alignableObj(hrDag0, hrdagListener())
//        val e2 = alignableObj(hrDag1, hrdagListener())
//
//        val alignment = e1.align(e2)!!
//        println(jacksonObjectMapper().writeValueAsString(alignment))
//
//        val applied = e1.applyAlignment(alignment.operations)
//        Assertions.assertTrue(e2.align(applied)!!.operations.isEmpty())
//    }
//}
