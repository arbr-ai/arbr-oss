package com.arbr.alignable.alignable.v2.dag//package com.arbr.alignable.alignable.v2.dag
//
//class TokenWindowDAGInducerTest {
//
////    private fun bitVector(z: Int): List<Int> {
////        var l = z
////        val bits = mutableListOf<Int>()
////        while (l > 0) {
////            bits.add(l % 2)
////            l /= 2
////        }
////        return bits
////    }
////
////    private fun stateString(state: AdjacencyMatrixDAGAlignmentTraversalState<*, *, *>): String {
////        val targetPath = bitVector(state.targetState).joinToString(", ") { it.toString() }
////        val sourcePath = bitVector(state.sourceState).joinToString(", ") { it.toString() }
////        return "TS[\n\tt=<$targetPath>,\n\ts=<$sourcePath>,\n\tq=<${state.constructionState}>\n${state.latestOperation}\n]"
////    }
////
////    private fun <V> matrixFromPartialOrderSet(partialOrderSet: PartialOrderSet<V>): List<List<Boolean>> {
////        val flatList = partialOrderSet.toFlatList(PartialOrderFlatteningScheme.DEPTH_FIRST)
////        val superiors = flatList.map {
////            partialOrderSet.superiorsOf(it).map { sup ->
////                flatList.indexOf(sup)
////            }
////        }
////
////        val matrix = superiors.map { row ->
////            flatList.indices.map { it in row }
////        }
////        return matrix
////    }
////
////    @Test
////    fun `aligns token window dag`() {
////        val patchN = 16
////        val docN = 16
////
////        val random = Random(seed = 10101223L)
////
////        // Generate lots of seeded random edits
////        // Size of this list is not related to runtime, it's to take a sample
////        val edits = (0 until 10).flatMap { i ->
////            (0 until 10).map { j ->
////                "$i,$j:" + random.nextInt().toString()
////            }
////        }
////
////        val patchOpsInDocReserved = min(patchN, max(patchN * 4 / 5, 2))
////        val patchOps = edits.shuffled(random).take(patchN).map {
////            AdjacencyMatrixDAGAlignmentHelperPosetTest.Edit(
////                UUID.randomUUID().toString(), listOf(it)
////            )
////        }
////
////        val reservedIndices = (0 until docN).shuffled(random).take(patchOpsInDocReserved)
////            .zip(patchOps)
////            .toMap()
////        val documentFreshOps = edits.shuffled(random).take(docN - patchOpsInDocReserved)
////
////        var jj = 0
////        val documentOps = (0 until docN).map { i ->
////            if (i in reservedIndices) {
////                reservedIndices[i]!!
////            } else {
////                AdjacencyMatrixDAGAlignmentHelperPosetTest.Edit(
////                    UUID.randomUUID().toString(),
////                    listOf(documentFreshOps[jj])
////                ).also { jj++ }
////            }
////        }
////
////        // Binary tree for simplicity
////        // Binary tree is worst case for runtime, worth thinking more about
////        // Interesting interview problem: generate all dags on n nodes efficiently with equal probability
////        val patchOrder = patchOps
////            .indices
////            .flatMap { i ->
////                if (i == 0) {
////                    emptyList()
////                } else {
////                    listOf(i / 2 to i)
////                }
////            }
////            .map { patchOps[it.first] to patchOps[it.second] }
////        val documentOrder = documentOps
////            .indices
////            .flatMap { i ->
////                if (i == 0) {
////                    emptyList()
////                } else {
////                    listOf(i / 2 to i)
////                }
////            }
////            .map { documentOps[it.first] to documentOps[it.second] }
////
////        val patchPoset = KeyedPartialOrderSet.ofChildMap(
////            patchOps.associateBy { it.key },
////            patchOrder.groupBy {
////                it.first.key
////            }.mapValues { (_, l) -> l.map { it.second.key } },
////        )
////
////        val documentPoset = KeyedPartialOrderSet.ofChildMap(
////            documentOps.associateBy { it.key },
////            documentOrder.groupBy {
////                it.first.key
////            }.mapValues { (_, l) -> l.map { it.second.key } },
////        )
////
////        val targetMatrix = matrixFromPartialOrderSet(patchPoset)
////        val targetValues = patchPoset.toFlatList(PartialOrderFlatteningScheme.DEPTH_FIRST)
////        val sourceMatrix = matrixFromPartialOrderSet(documentPoset)
////        val sourceValues = documentPoset.toFlatList(PartialOrderFlatteningScheme.DEPTH_FIRST)
////
////        val matrixConfig = AdjacencyMatrixDAGAlignmentConfiguration(
////            mergeCost = 1.0,
////            induceCost = 2.5,
////            deduceCost = 2.5,
////            rootNodeCost = 1.0,
////            childNodeCost = 0.5,
////        )
////
////        val inducer = TokenWindowTestInducer()
////
////        val (path, cost) = AdjacencyMatrixDAGAlignmentHelper(
////            AdjacencyMatrixDAGValued(targetMatrix, targetValues),
////            AdjacencyMatrixDAGValued(sourceMatrix, sourceValues),
////            inducer,
////            matrixConfig,
////        ).alignmentPath()!!
////
////        println(path.map { stateString(it) })
////        println(cost)
////    }
//
//}