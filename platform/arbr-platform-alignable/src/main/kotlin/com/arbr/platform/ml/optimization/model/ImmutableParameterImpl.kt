package com.arbr.platform.ml.optimization.model

import com.arbr.platform.ml.optimization.base.ImmutableParameter

data class ImmutableParameterImpl<T>(
    override val value: T
): ImmutableParameter<T>