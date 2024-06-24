package com.arbr.platform.ml.linear.typed.impl

import com.arbr.platform.ml.linear.ejml.EJMLPrincipalComponentAnalysis
import com.arbr.platform.ml.linear.typed.base.ColumnVector
import com.arbr.platform.ml.linear.typed.base.Matrix
import com.arbr.platform.ml.linear.typed.base.Vector
import com.arbr.platform.ml.linear.typed.shape.Dim
import com.arbr.util.invariants.Invariants
import com.arbr.util.collections.mapToArray
import org.ejml.simple.SimpleMatrix
import java.util.*

object TypedMatrices {

    /**
     * Special constructors
     */

    fun <M : Dim, N : Dim> of(
        shapeM: M,
        shapeN: N,
        rowBuilder: SimpleBuilder.() -> Unit,
    ): TypedMatrix<M, N> = SimpleBuilder().also(rowBuilder).build(shapeM, shapeN)

    fun <M : Dim> diag(
        values: DoubleArray,
        shape: M,
    ): TypedMatrix<M, M> = TypedMatrix(SimpleMatrix.diag(*values), shape, shape)

    fun <M : Dim> diag(
        values: Vector<*, *, *, *, M>,
        shape: M,
    ): TypedMatrix<M, M> = TypedMatrix(SimpleMatrix.diag(*values.asFlatArray()), shape, shape)

    /**
     * Row / column getters + manipulations
     */

    fun <M : Dim, N : Dim> getRows(
        matrix: Matrix<M, N>,
        rowIndices: List<Int>,
    ): Matrix<Dim.AtMost<M>, N> {
        val firstIndex = rowIndices.firstOrNull() ?: return TypedMatrix.empty(
            0,
            matrix.numColumns,
            object : Dim.AtMost<M> {},
            matrix.numColsShape,
        )
        val firstMatrix = matrix.getRow(firstIndex)
        val otherMatrices = rowIndices.drop(1).map { matrix.getRow(it) }

        return TypedMatrix(
            firstMatrix.asSimpleMatrix().concatRows(
                *otherMatrices
                    .map { it.asSimpleMatrix() }
                    .toTypedArray()
            ),
            object : Dim.AtMost<M> {},
            matrix.numColsShape,
        )
    }

    fun <M : Dim> getRows(
        matrix: ColumnVector<M>,
        rowIndices: List<Int>,
    ): ColumnVector<Dim.AtMost<M>> {
        val dim = object : Dim.AtMost<M> {}
        val firstIndex = rowIndices.firstOrNull() ?: return TypedColumnVector(SimpleMatrix(0, 1), dim)
        val firstMatrix = matrix.getRow(firstIndex)
        val otherMatrices = rowIndices.drop(1).map { matrix.getRow(it) }

        return TypedColumnVector(
            firstMatrix.asSimpleMatrix().concatRows(
                *otherMatrices
                    .map { it.asSimpleMatrix() }
                    .toTypedArray()
            ),
            dim,
        )
    }

    fun <M : Dim, N : Dim> normalizeRows(
        matrix: Matrix<M, N>
    ): Matrix<M, N> {
        if (matrix.numRows == 0) {
            return matrix
        }

        val firstRow = matrix.getRow(0).let { row ->
            val norm = row.normF()
            if (norm == 0.0) {
                row
            } else {
                row.scale(1.0 / norm)
            }
        }
        val remainingRows = Array(matrix.numRows - 1) { i ->
            val row = matrix.getRow(i + 1)
            val norm = row.normF()
            if (norm == 0.0) {
                row
            } else {
                row.scale(1.0 / norm)
            }
        }

        return firstRow.concatRows(matrix.numRowsShape, *remainingRows)
    }

    /**
     * Dataset manipulations
     */

    fun <M : Dim, N : Dim, T> makeSample(
        source: List<T>,
        vectorize: (Int, T) -> Pair<DoubleArray, Double>, // Pair of input values, output value
        shapeM: M,
        shapeN: N,
    ): Sample<M, N> {
        val pairs = source.mapIndexed(vectorize)
        return Sample(
            TypedMatrix(SimpleMatrix(pairs.mapToArray { it.first }), shapeM, shapeN),
            TypedColumnVector(SimpleMatrix(pairs.mapToArray { doubleArrayOf(it.second) }), shapeM),
        )
    }

    /**
     * Concatenate a non-empty list of samples by row.
     */
    fun <M : Dim, N : Dim> concatSamples(
        samples: List<Sample<M, N>>
    ): Sample<Dim.AtLeast<M>, N> {
        val firstSample = samples.firstOrNull() ?: throw Exception("Attempted to concatenate an empty list of samples")
        val remainingSamples = samples.drop(1)
        val remainingInputs = remainingSamples.mapToArray { it.inputs }
        val remainingOutputs = remainingSamples.mapToArray { it.outputs }

        return Sample(
            firstSample.inputs.concatRows(*remainingInputs),
            firstSample.outputs.concatRows(*remainingOutputs),
        )
    }

    fun <M : Dim, N : Dim> trainTestSplitByCount(
        sample: Sample<M, N>,
        numTrain: Int,
        numTest: Int,
        random: Random,
    ): SplitSample<M, N> {
        Invariants.check {
            require(numTrain >= 0 && numTest >= 0)
        }

        val totalSize = numTrain + numTest
        val entireIndexRange = (0 until totalSize).shuffled(random)
        val trainIndices = entireIndexRange.take(numTrain)
        val testIndices = entireIndexRange.drop(numTrain)

        val trainSample = Sample(
            getRows(sample.inputs, trainIndices),
            getRows(sample.outputs, trainIndices),
        )
        val testSample = Sample(
            getRows(sample.inputs, testIndices),
            getRows(sample.outputs, testIndices),
        )
        return SplitSample(trainSample, testSample)
    }

    /**
     * Make a train-test split sample from a data source.
     * Some duplication with `trainTestSplitByCount` to avoid vectorizing the entire input.
     */
    fun <M : Dim.AtMost<M>, N : Dim, T> makeSplitSample(
        source: List<T>,
        numTrain: Int,
        numTest: Int,
        random: Random,
        shapeM: M,
        shapeN: N,
        vectorize: (T) -> Pair<DoubleArray, Double>, // Pair of input values, output value
    ): SplitSample<M, N> {
        // TODO: Handle 0 more gracefully
        Invariants.check {
            require(numTrain > 0 && numTest > 0)
        }

        val totalSize = numTrain + numTest

        Invariants.check {
            require(source.size >= totalSize)
        }

        val entireIndexRange = (0 until totalSize).shuffled(random)
        val trainIndices = entireIndexRange.take(numTrain)
        val testIndices = entireIndexRange.drop(numTrain)

        val trainPairs = trainIndices.map { vectorize(source[it]) }
        val trainSample = Sample(
            TypedMatrix<M, N>(trainPairs.mapToArray { it.first }, shapeM, shapeN),
            TypedColumnVector(trainPairs.mapToArray { doubleArrayOf(it.second) }, shapeM),
        )

        val testPairs = testIndices.map { vectorize(source[it]) }
        val testSample = Sample(
            TypedMatrix<M, N>(testPairs.mapToArray { it.first }, shapeM, shapeN),
            TypedColumnVector(testPairs.mapToArray { doubleArrayOf(it.second) }, shapeM),
        )

        return SplitSample(trainSample, testSample)
    }

    fun <M : Dim, N : Dim> linearRegression(sample: Sample<M, N>): ColumnVector<N> {
        // Compute the pseudo-inverse of the design matrix
        val sampleInputsPseudoInverse = sample.inputs.asSimpleMatrix().pseudoInverse()

        // Multiply the pseudo-inverse by the outcome vector to obtain the regression coefficients
        val simpleMatrixLinearRegression = sampleInputsPseudoInverse.mult(sample.outputs.asSimpleMatrix())

        return TypedColumnVector(simpleMatrixLinearRegression, sample.inputs.numColsShape)
    }

    fun <M : Dim, N : Dim> principalComponents(
        matrix: Matrix<M, N>,
        numComponents: Int,
    ): Matrix<M, N> {
        val pca = EJMLPrincipalComponentAnalysis()
        pca.setup(matrix.numRows, matrix.numColumns)
        val matrixArr = matrix.asArray()
        for (row in matrixArr) {
            pca.addSample(row)
        }
        pca.computeBasis(numComponents)

        val componentVectors = (0 until numComponents).map { i ->
            pca.getBasisVector(i)
        }.toTypedArray()
        return normalizeRows(TypedMatrix(componentVectors, matrix.numRowsShape, matrix.numColsShape))
    }

    fun <M : Dim, N : Dim> principalComponentProjection(
        matrix: Matrix<M, N>,
        numComponents: Int,
    ): Matrix<M, M> {
        val components = principalComponents(matrix, numComponents)
        return matrix.mult(components.transpose())
    }

    /**
     * Classes
     */

    data class Sample<M : Dim, N : Dim>(
        /**
         * m x n matrix of input values
         */
        val inputs: Matrix<M, N>,

        /**
         * m x 1 vector of observations
         */
        val outputs: ColumnVector<M>,
    )

    data class SplitSample<M : Dim, N : Dim>(
        val train: Sample<out Dim.AtMost<M>, N>,
        val test: Sample<out Dim.AtMost<M>, N>,
    )

    class SimpleBuilder {
        private var n: Int? = null
        private val rows = mutableListOf<DoubleArray>()

        private fun rowFromArray(values: DoubleArray) {
            val rowLength = values.size
            if (n != null && rowLength != n) {
                throw IllegalArgumentException("Matrix row size mismatch: Expected $n, got $rowLength")
            }
            rows.add(values)
            if (n == null) {
                n = rowLength
            }
        }

        fun row(vararg values: Double) = rowFromArray(values)

        fun r(vararg values: Double) = rowFromArray(values)

        fun <M : Dim, N : Dim> build(shapeM: M, shapeN: N): TypedMatrix<M, N> {
            return TypedMatrix(
                SimpleMatrix(
                    rows.toTypedArray()
                ),
                shapeM,
                shapeN,
            )
        }
    }

}