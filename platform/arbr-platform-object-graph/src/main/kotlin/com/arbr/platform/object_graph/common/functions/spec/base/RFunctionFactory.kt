package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.naming.NamedResource
import com.arbr.object_model.core.types.naming.NamedResourceKey

abstract class RFunctionFactory {
    abstract fun <RV : ResourceView<R>, R: NamedResource<*, RK, *, *>, RK: NamedResourceKey> actingOnResource(
        resourceViewClass: Class<RV>,
        configure: RFunctionSpecifier<RV>.() -> Unit
    ): RFunctionConfigurableSetDelegateProvider<RV, RK>

    inline fun <reified RV : ResourceView<R>, R: NamedResource<*, RK, *, *>, RK: NamedResourceKey> RFunctionFactory.actingOnResource(
        noinline configure: RFunctionSpecifier<RV>.() -> Unit,
    ): RFunctionConfigurableSetDelegateProvider<RV, RK> {
        return actingOnResource(RV::class.java, configure)
    }
}
