package com.arbr.platform.ml.linear.ejml

import org.ejml.data.DMatrixRMaj
import org.ejml.dense.row.CommonOps_DDRM
import org.ejml.dense.row.NormOps_DDRM
import org.ejml.dense.row.SingularOps_DDRM
import org.ejml.dense.row.factory.DecompositionFactory_DDRM
import org.ejml.interfaces.decomposition.SingularValueDecomposition

/**
 * Adapted from (https://github.com/lejon/T-SNE-Java/blob/master/tsne-core/src/main/java/com/jujutsu/tsne/PrincipalComponentAnalysis.java)
 *
 * The following is a simple example of how to perform basic principal component analysis in EJML.
 *
 *
 *
 *
 * Principal Component Analysis (PCA) is typically used to develop a linear model for a set of data
 * (e.g. face images) which can then be used to test for membership.  PCA works by converting the
 * set of data to a new basis that is a subspace of the original set.  The subspace is selected
 * to maximize information.
 *
 *
 *
 * PCA is typically derived as an eigenvalue problem.  However in this implementation [SVD][org.ejml.interfaces.decomposition.SingularValueDecomposition]
 * is used instead because it will produce a more numerically stable solution.  Computation using EVD requires explicitly
 * computing the variance of each sample set. The variance is computed by squaring the residual, which can
 * cause loss of precision.
 *
 *
 *
 *
 * Usage:<br></br>
 * 1) call setup()<br></br>
 * 2) For each sample (e.g. an image ) call addSample()<br></br>
 * 3) After all the samples have been added call computeBasis()<br></br>
 * 4) Call  sampleToEigenSpace() , eigenToSampleSpace() , errorMembership() , response()
 *
 *
 * @author Peter Abeles
 */
class EJMLPrincipalComponentAnalysis {
    // principal component subspace is stored in the rows
    private var V_t: DMatrixRMaj? = null

    // how many principal components are used
    private var numComponents = 0

    // where the data is stored
    private val A = DMatrixRMaj(1, 1)
    private var sampleIndex = 0

    // mean values of each element across all the samples
    lateinit var mean: DoubleArray

    /**
     * Must be called before any other functions. Declares and sets up internal data structures.
     *
     * @param numSamples Number of samples that will be processed.
     * @param sampleSize Number of elements in each sample.
     */
    fun setup(numSamples: Int, sampleSize: Int) {
        mean = DoubleArray(sampleSize)
        A.reshape(numSamples, sampleSize, false)
        sampleIndex = 0
        numComponents = -1
    }

    /**
     * Adds a new sample of the raw data to internal data structure for later processing.  All the samples
     * must be added before computeBasis is called.
     *
     * @param sampleData Sample from original raw data.
     */
    fun addSample(sampleData: DoubleArray) {
        require(A.getNumCols() == sampleData.size) { "Unexpected sample size" }
        require(sampleIndex < A.getNumRows()) { "Too many samples" }
        for (i in sampleData.indices) {
            A[sampleIndex, i] = sampleData[i]
        }
        sampleIndex++
    }

    /**
     * Computes a basis (the principal components) from the most dominant eigenvectors.
     *
     * @param numComponents Number of vectors it will use to describe the data.  Typically much
     * smaller than the number of elements in the input vector.
     */
    fun computeBasis(numComponents: Int) {
        require(numComponents <= A.getNumCols()) { "More components requested that the data's length." }
        require(sampleIndex == A.getNumRows()) { "Not all the data has been added" }
        require(numComponents <= sampleIndex) { "More data needed to compute the desired number of components" }
        this.numComponents = numComponents

        // compute the mean of all the samples
        for (i in 0 until A.getNumRows()) {
            for (j in mean.indices) {
                mean[j] += A[i, j]
            }
        }
        for (j in mean.indices) {
            mean[j] /= A.getNumRows().toDouble()
        }

        // subtract the mean from the original data
        for (i in 0 until A.getNumRows()) {
            for (j in mean.indices) {
                A[i, j] = A[i, j] - mean[j]
            }
        }

        // Compute SVD and save time by not computing U
        val svd: SingularValueDecomposition<DMatrixRMaj> =
            DecompositionFactory_DDRM.svd(A.numRows, A.numCols, false, true, false)
        if (!svd.decompose(A)) throw RuntimeException("SVD failed")
        V_t = svd.getV(null, true)
        val W: DMatrixRMaj = svd.getW(null)

        // Singular values are in an arbitrary order initially
        SingularOps_DDRM.descendingOrder(null, false, W, V_t, true)

        // strip off unneeded components and find the basis
        V_t!!.reshape(numComponents, mean.size, true)
    }

    /**
     * Returns a vector from the PCA's basis.
     *
     * @param which Which component's vector is to be returned.
     * @return Vector from the PCA basis.
     */
    fun getBasisVector(which: Int): DoubleArray {
        require(!(which < 0 || which >= numComponents)) { "Invalid component" }
        val v = DMatrixRMaj(1, A.numCols)
        CommonOps_DDRM.extract(V_t, which, which + 1, 0, A.numCols, v, 0, 0)
        return v.data
    }

    /**
     * Converts a vector from sample space into eigen space.
     *
     * @param sampleData Sample space data.
     * @return Eigen space projection.
     */
    fun sampleToEigenSpace(sampleData: DoubleArray): DoubleArray {
        require(sampleData.size == A.getNumCols()) { "Unexpected sample length" }
        val mean = DMatrixRMaj.wrap(A.getNumCols(), 1, mean)
        val s = DMatrixRMaj(A.getNumCols(), 1, true, *sampleData)
        val r = DMatrixRMaj(numComponents, 1)
        CommonOps_DDRM.subtract(s, mean, s)
        CommonOps_DDRM.mult(V_t, s, r)
        return r.data
    }

    /**
     * Converts a vector from eigen space into sample space.
     *
     * @param eigenData Eigen space data.
     * @return Sample space projection.
     */
    fun eigenToSampleSpace(eigenData: DoubleArray): DoubleArray {
        require(eigenData.size == numComponents) { "Unexpected sample length" }
        val s = DMatrixRMaj(A.getNumCols(), 1)
        val r = DMatrixRMaj.wrap(numComponents, 1, eigenData)
        CommonOps_DDRM.multTransA(V_t, r, s)
        val mean = DMatrixRMaj.wrap(A.getNumCols(), 1, mean)
        CommonOps_DDRM.add(s, mean, s)
        return s.data
    }

    /**
     *
     *
     * The membership error for a sample.  If the error is less than a threshold then
     * it can be considered a member.  The threshold's value depends on the data set.
     *
     *
     *
     * The error is computed by projecting the sample into eigenspace then projecting
     * it back into sample space and
     *
     *
     * @param sampleA The sample whose membership status is being considered.
     * @return Its membership error.
     */
    fun errorMembership(sampleA: DoubleArray): Double {
        val eig = sampleToEigenSpace(sampleA)
        val reproj = eigenToSampleSpace(eig)
        var total = 0.0
        for (i in reproj.indices) {
            val d = sampleA[i] - reproj[i]
            total += d * d
        }
        return Math.sqrt(total)
    }

    /**
     * Computes the dot product of each basis vector against the sample.  Can be used as a measure
     * for membership in the training sample set.  High values correspond to a better fit.
     *
     * @param sample Sample of original data.
     * @return Higher value indicates it is more likely to be a member of input dataset.
     */
    fun response(sample: DoubleArray): Double {
        require(sample.size == A.numCols) { "Expected input vector to be in sample space" }
        val dots = DMatrixRMaj(numComponents, 1)
        val s = DMatrixRMaj.wrap(A.numCols, 1, sample)
        CommonOps_DDRM.mult(V_t, s, dots)
        return NormOps_DDRM.normF(dots)
    }
}