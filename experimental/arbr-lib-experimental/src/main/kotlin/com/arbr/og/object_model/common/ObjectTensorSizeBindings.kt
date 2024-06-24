package com.arbr.og.object_model.common

import com.arbr.platform.ml.linear.typed.shape.Dim

object ObjectTensorSizeBindings {

    fun bind(dim: Dim): Int {
        return when (dim) {
            Dim.VariableF -> System.getProperty("com.arbr.tensors.size_bindings.F").toInt()
            else -> throw NotImplementedError()
        }
    }
}