package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import kotlin.reflect.KProperty

class RValueFunctionConfigurableDelegateProvider<RV : ResourceView<*>, T>(
    private val resourceViewClass: Class<RV>,
    private val provide: (name: String) -> RValueFunctionConfigurable<RV, T>,
) {
    private var configured: RValueFunctionConfigurable<RV, T>? = null
    private var getConfiguration: (() -> RValueFunctionConfigurable<RV, T>)? = null

    @Synchronized
    fun getOrConfigure(): RValueFunctionConfigurable<RV, T> {
        val currentConfigured = configured
        return if (currentConfigured == null) {
            val get = getConfiguration ?: throw IllegalStateException()
            get().also { this.configured = it }
        } else {
            currentConfigured
        }
    }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Delegate<RValueFunctionConfigurable<RV, T>> {
        return synchronized(this) {
            val functionName = property.name
            println("[RValueFunction provideDelegate] Creating function via property: $functionName")
            val delegate = Delegate {
                provide(functionName)
            }
            this.getConfiguration = { provide(functionName) }
            delegate
        }
    }
}
