package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView
import com.arbr.object_model.core.types.naming.NamedResourceKey
import kotlin.reflect.KProperty

class RFunctionConfigurableSetDelegateProvider<RV : ResourceView<*>, RK: NamedResourceKey>(
    
    private val configure: (functionSetName: String) -> RFunctionConfigurableSet<RV, RK>,
) {
    private var configured: RFunctionConfigurableSet<RV, RK>? = null
    private var getConfiguration: (() -> RFunctionConfigurableSet<RV, RK>)? = null

    @Synchronized
    fun getOrConfigure(): RFunctionConfigurableSet<RV, RK> {
        val currentConfigured = configured
        return if (currentConfigured == null) {
            val get = getConfiguration ?: throw IllegalStateException()
            get().also { this.configured = it }
        } else {
            currentConfigured
        }
    }

    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Delegate<RFunctionConfigurableSet<RV, RK>> {
        return synchronized(this) {
            val functionSetName = property.name
            println("[RFunctionSet provideDelegate] Creating resource action via property: $functionSetName")
            val delegate = Delegate {
                configure(functionSetName)
            }
            this.getConfiguration = { configure(functionSetName) }
            delegate
        }
    }
}