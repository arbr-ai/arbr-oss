package com.arbr.alignable.alignable.v2.dag

class AdjacencyMatrixDAGPowerLatticeNodeLabels(
    val nodeLabels: Array<PowerLatticeVector?>,
) {

    override fun toString(): String {
        val rowsString = nodeLabels.joinToString("\n") { r ->
            r.toString()
        }

        return "[\n$rowsString\n]"
    }

    companion object {


        fun ofDagSimpleDependencies(adjacencyMatrixDAG: AdjacencyMatrixDAGValued<*>): AdjacencyMatrixDAGPowerLatticeNodeLabels {
            val n = adjacencyMatrixDAG.matrix.size
            val labels = Array<PowerLatticeVector?>(n) {
                null
            }

            // Leaves: i : A[i, *] = 0
            val remainingIndices = (0 until n).toMutableSet()

            while (remainingIndices.isNotEmpty()) {
                val newlyVisited = mutableSetOf<Int>()

                for (j in remainingIndices) {

                    var isEligible = true
                    val plvArray = Array(n) { 0 }
                    for (i in 0 until n) {
                        val isChild = adjacencyMatrixDAG.matrix[i][j]

                        if (!isChild) {
                            continue
                        }

                        val parentPlv = labels[i]
                        if (parentPlv == null) {
                            isEligible = false
                            break
                        }

                        for ((k, pk) in parentPlv.powerArray.withIndex()) {
                            plvArray[k] += pk
                        }
                    }

                    if (!isEligible) {
                        continue
                    }

                    println("$j eligible")
                    plvArray[j] = 1

                    newlyVisited.add(j)

                    // Set inbound edge labels
                    val plv = PowerLatticeVector(plvArray)
                    labels[j] = plv
                    println("$j  $plv")
                }

                remainingIndices.removeAll(newlyVisited)
            }

            return AdjacencyMatrixDAGPowerLatticeNodeLabels(labels)
        }
    }
}