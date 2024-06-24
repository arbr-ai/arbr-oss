package com.arbr.og.object_model.common.functions.platform

import com.arbr.object_model.core.types.ResourceView
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.functions.spec.base.RFunctionConfigurable
import com.arbr.og.object_model.common.properties.NonNullRequirementsProvider
import com.arbr.og.object_model.common.values.SourcedValue

abstract class ResourceFunctionContext {
    abstract val resourceViewInstantiators: ResourceViewInstantiators
    abstract val nonNullRequirementsProvider: NonNullRequirementsProvider

    fun <V : Any, T : ObjectModel.ObjectValue<V?, *, *, T>> T.nonnull(): SourcedValue<V> {
        return nonNullRequirementsProvider.getNonNullValue(this)
    }

    fun <RV : ResourceView<*>> new(resourceViewClass: Class<RV>): RV {
        return resourceViewInstantiators.instantiator<RV>(resourceViewClass).newResource()
    }

    fun <RV : ResourceView<*>> new(resourceViewClass: Class<RV>, configure: RV.() -> Unit): RV {
        return resourceViewInstantiators.instantiator<RV>(resourceViewClass).newResource().apply(configure)
    }

    inline fun <reified RV : ResourceView<*>> new(): RV {
        return new(RV::class.java)
    }

    inline fun <reified RV : ResourceView<*>> new(noinline configure: RV.() -> Unit): RV {
        return new(RV::class.java, configure)
    }

    operator fun <RV : ResourceView<*>> RFunctionConfigurable<RV>.invoke() {
        this.configure(this@ResourceFunctionContext)
    }
}
