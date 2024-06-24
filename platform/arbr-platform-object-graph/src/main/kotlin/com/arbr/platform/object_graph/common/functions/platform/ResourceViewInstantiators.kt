package com.arbr.og.object_model.common.functions.platform

import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.naming.NamedResourceKey

interface ResourceViewInstantiators {

    fun <RV : ResourceView<*>> instantiator(
        resourceViewClass: Class<out ResourceView<*>>
    ): ResourceViewInstantiator<RV>

}

inline fun <reified RV : ResourceView<*>> ResourceViewInstantiators.instantiator(): ResourceViewInstantiator<RV> {
    return instantiator(RV::class.java)
}

/**
 * ResourceViewInstantiators restricted to a single schema as dictated by the RK
 * Can't enforce limited scope easily
 *
 * Global instantiators, above, delegate to domain instantiators, which in turn construct the appropriate RV
 * instantiator individually
 */
interface ResourceViewDomainInstantiators<RK : NamedResourceKey> {

    fun <RV : ResourceView<*>> instantiator(
        resourceViewClass: Class<out ResourceView<*>>
    ): ResourceViewInstantiator<RV>

}
