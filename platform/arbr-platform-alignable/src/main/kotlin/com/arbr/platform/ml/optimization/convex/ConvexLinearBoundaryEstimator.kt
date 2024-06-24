package com.arbr.platform.ml.optimization.convex

import com.arbr.platform.ml.optimization.model.AsyncBoundaryEvaluation
import com.arbr.platform.ml.optimization.model.AsyncBoundarySimpleEvaluation
import org.slf4j.LoggerFactory
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToLong

class ConvexLinearBoundaryEstimator(
    private val globalMinValue: Double,
    private val globalMaxValue: Double,
) {

    private val singleValueGapThreshold = 0.25

    @Suppress("SameParameterValue")
    private fun computeHistogramPeakIntervals(
        values: List<Double>,
        resolution: Double,
        minSeparation: Double,
        maxN: Int,
    ): List<Double> {
        val bucketSizeMap = values.groupBy { (it / resolution).toInt() }.mapValues { it.value.size }
        val resolutionIndexes = bucketSizeMap.keys.sorted()
        val bucketSizes = resolutionIndexes.map { bucketSizeMap[it]!! }

        val localMaxima = (1 until bucketSizes.size - 1).mapNotNull { i ->
            if (bucketSizes[i - 1] <= bucketSizes[i] && bucketSizes[i] >= bucketSizes[i + 1]) {
                bucketSizes[i] to (resolution * resolutionIndexes[i])
            } else {
                null
            }
        }

        val descendingPeaks = localMaxima
            .sortedByDescending { it.first } // Sort desc by associated peak size

        val result = mutableListOf<Double>()
        for ((_, value) in descendingPeaks) {
            if (result.none { r -> abs(r - value) < minSeparation }) {
                result.add(value)
            }

            if (result.size >= maxN) {
                break
            }
        }

        return result
    }

    private fun contradictionCoef(
        d: Double,
    ): Double {
        return if (d < 0) {
            0.0
        } else {
            d
        }
    }

    private fun estimateBoundaries(
        sample: List<Pair<Double, Boolean>>,
    ): List<Double> {
        val sampleValues = sample.map { it.first }.sorted()
        val passed = sample.filter { it.second }.map { it.first }
        val failed = sample.filterNot { it.second }.map { it.first }

        val midpoints = sampleValues.zip(sampleValues.drop(1)).map { (it.first + it.second) / 2.0 }

        val midpointScores = midpoints.map { k ->
            val leftFailedWeight = failed.sumOf { x ->
                if (x >= k) {
                    0.0
                } else {
                    1.0 / abs(x - k)
                }
            }
            val leftPassedWeight = passed.sumOf { x ->
                if (x >= k) {
                    0.0
                } else {
                    1.0 / abs(x - k)
                }
            }

            val rightFailedWeight = failed.sumOf { x ->
                if (x <= k) {
                    0.0
                } else {
                    1.0 / abs(x - k)
                }
            }
            val rightPassedWeight = passed.sumOf { x ->
                if (x <= k) {
                    0.0
                } else {
                    1.0 / abs(x - k)
                }
            }

            max(leftFailedWeight + rightPassedWeight, leftPassedWeight + rightFailedWeight)
        }

        val localMaximumRadius = 2

        val localMaxima = midpointScores.withIndex().filter { (index, score) ->
            if (index < localMaximumRadius || index >= midpointScores.size - localMaximumRadius) {
                false
            } else {
                ((index - localMaximumRadius) until index).all { midpointScores[it] < score } && ((index + 1) until (index + localMaximumRadius + 1)).all { midpointScores[it] < score }
            }
        }.map { midpoints[it.index] }

        return localMaxima
    }

    private fun estimateBoundary(
        sample: List<Pair<Double, Boolean>>,
    ): Double {
        val sampleValues = sample.map { it.first }.sorted()
        val passed = sample.filter { it.second }.map { it.first }
        val failed = sample.filterNot { it.second }.map { it.first }

        if (passed.isEmpty() && failed.isEmpty()) {
            return 0.0
        } else if (passed.isEmpty()) {
            return failed.sum() / failed.size
        } else if (failed.isEmpty()) {
            return passed.sum() / passed.size
        }

        val failedMean = failed.sum() / failed.size
        val passedMean = passed.sum() / passed.size
        val directionality = if (passedMean >= failedMean) 1.0 else -1.0

        val midpoints = sampleValues.zip(sampleValues.drop(1)).map { (it.first + it.second) / 2.0 }

        val bestMidpoint = midpoints.minBy { k ->
            val counterWeight = sample.sumOf { (y, passed) ->
                // y > k and positive directionality -> corroborator -> negative arg
                val passedCoef = if (passed) 1.0 else -1.0
                val contradictionArg = (k - y) * directionality * passedCoef
                val pullDirectionCoef = if (y < k) -1.0 else 1.0
                contradictionCoef(contradictionArg) * pullDirectionCoef
            }

            abs(counterWeight)
        }

        return bestMidpoint
    }

    private fun getDiameterAndDirection(
        sampledValues: List<Pair<Double, Boolean>>,
        point: Double,
    ): Pair<Double?, Boolean?> {
        val greatestLeftPassed = sampledValues.filter { it.second && it.first < point }.maxByOrNull { it.first }?.first
        val greatestLeftFailed = sampledValues.filter { !it.second && it.first < point }.maxByOrNull { it.first }?.first

        val leastRightPassed = sampledValues.filter { it.second && it.first > point }.minByOrNull { it.first }?.first
        val leastRightFailed = sampledValues.filter { !it.second && it.first > point }.minByOrNull { it.first }?.first

        val (smallestDiameter, directionIsPositive) = if (greatestLeftPassed != null && leastRightFailed != null) {
            if (greatestLeftFailed != null && greatestLeftFailed > greatestLeftPassed) {
                // Interior contradiction
                null to null
            } else if (leastRightPassed != null && leastRightPassed < leastRightFailed) {
                // Interior contradiction
                null to null
            } else {
                (leastRightFailed - greatestLeftPassed) to false
            }
        } else if (greatestLeftFailed != null && leastRightPassed != null) {
            if (greatestLeftPassed != null && greatestLeftPassed > greatestLeftFailed) {
                // Interior contradiction
                null to null
            } else if (leastRightFailed != null && leastRightFailed < leastRightPassed) {
                // Interior contradiction
                null to null
            } else {
                (leastRightPassed - greatestLeftFailed) to true
            }
        } else {
            null to null
        }

        return smallestDiameter to directionIsPositive
    }

    fun switchPoints(
        combinedSample: List<Pair<Double, Boolean>>,
    ): List<Double> {
        val sortedSample = combinedSample.sortedBy { it.first }

        return sortedSample.zip(sortedSample.drop(1)).mapNotNull { (p0, p1) ->
            if (p0.second != p1.second) {
                (p0.first + p1.first) / 2.0
            } else {
                null
            }
        }
    }

    private fun singleValueTestPoints(combinedSample: List<Pair<Double, Boolean>>): List<Pair<Double, Double?>> {
        val pointValues = combinedSample
            .map { it.first }
            .sorted()

        val quantizationUnit = 1E-5

        val testValues = if (globalMinValue in pointValues && globalMaxValue in pointValues) {
            logger.info("Sourcing single outcome interior frontier")

            val unexploredMidPoints = pointValues.zip(pointValues.drop(1)).mapNotNull { (a, b) ->
                if (b - a < singleValueGapThreshold) {
                    null
                } else {
                    (a + b) / 2.0
                }
            }

            if (unexploredMidPoints.isEmpty()) {
                logger.info("Maxing out value due to single value over saturated interval")
                listOf(globalMaxValue to 0.0)
            } else {
                unexploredMidPoints.map { it to null }
            }
        } else {
            logger.info("Sourcing single outcome frontier towards boundary")

            val rangeMin = pointValues.min()
            val rangeMax = pointValues.max()

            val lo = (globalMinValue + rangeMin) / 2.0
            val hi = (globalMaxValue + rangeMax) / 2.0

            listOf(globalMinValue, lo, hi, globalMaxValue).filter { it !in pointValues }
                .map { it to null }
        }
            .map { (x, diameter) ->
                val xq = quantizationUnit * (x / quantizationUnit).roundToLong()
                xq to diameter
            }
            .distinct()
            .filter { (x, diameter) ->
                diameter != null || pointValues.none { y ->
                    abs(x - y) < 2 * quantizationUnit
                }
            }

        return testValues.ifEmpty { listOf(globalMaxValue to 0.0) }
    }

    fun computeBoundaryIteration(
        priorSample: List<Pair<Double, AsyncBoundarySimpleEvaluation>>,
        currentSample: List<Pair<Double, AsyncBoundaryEvaluation>>,
    ): List<Pair<Double, Double?>> {
        val sample = currentSample.map { it.first to AsyncBoundarySimpleEvaluation(it.second.passed, it.second.score) }

        val combinedSample = priorSample + sample
        val combinedSamplePassed = combinedSample.map { it.first to it.second.passed }

        if (combinedSample.isNotEmpty() && (combinedSample.all { it.second.passed } || combinedSample.all { !it.second.passed })) {
            return singleValueTestPoints(combinedSamplePassed)
        }

        val boundaryDiameterFinishThreshold = 0.01
        val peakValueBucketResolution = 0.001
        val quantizationUnit = 1E-5

        val switchPoints = switchPoints(combinedSamplePassed)

        val combined = switchPoints.ifEmpty {
            logger.info("No switch points, falling back to nsect + peak")
            val thresholdsInRange = currentSample
                .flatMap { it.second.computedDecisionThresholds }
                .filter {
                    it >= globalMinValue && it < globalMaxValue
                }

            val nsectSample: List<Double> = estimateBoundaries(combinedSamplePassed) + listOf(estimateBoundary(combinedSamplePassed))
            val peakIntervals: List<Double> = computeHistogramPeakIntervals(
                thresholdsInRange,
                peakValueBucketResolution,
                minSeparation = 0.1,
                maxN = 2,
            )

            nsectSample + peakIntervals
        }
            .map {
                val (diameter, _) = getDiameterAndDirection(combinedSamplePassed, it)
                it to diameter
            }
            .onEach { (boundary, diameter) ->
                logger.debug("Got estimated boundary $boundary , diameter $diameter")
            }



        val allSampled = priorSample.map { it.first } + currentSample.map { it.first }

        /*
            Low diameter = succeed
            High diameter = keep trying to converge
            null diameter = ignore
         */
        return combined
            .filter { (x, diameter) ->
                diameter != null || allSampled.none { y ->
                    abs(x - y) < boundaryDiameterFinishThreshold
                }
            }
            .map { (x, diameter) ->
                val xq = quantizationUnit * (x / quantizationUnit).roundToLong()
                xq to diameter
            }
            .distinct()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ConvexLinearBoundaryEstimator::class.java)
    }

}