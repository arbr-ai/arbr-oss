package com.arbr.platform.ml.optimization.base

import com.arbr.platform.ml.optimization.model.BindingParameter
import java.io.File

interface Optimizer {

    fun optimize(
        parameters: List<BindingParameter<Double>>,
        eval: (List<BindingParameter<Double>>) -> Double,
        learningRate: Double = 0.01,
        tolerance: Double = 1e-6,
        maxIterations: Int = 1000,
        destination: File? = null,
    ): List<BindingParameter<Double>>

}