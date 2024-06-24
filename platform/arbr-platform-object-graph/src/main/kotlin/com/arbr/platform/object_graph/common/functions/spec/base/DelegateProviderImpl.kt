package com.arbr.og.object_model.common.functions.spec.base

import kotlin.reflect.KProperty

open class DelegateProviderImpl<T>(
    private val provide: (property: KProperty<*>) -> T,
): DelegateProvider<T> {
    private var configured: T? = null
    private var getConfiguration: (() -> T)? = null

    @Synchronized
    override fun getOrConfigure(): T {
        val currentConfigured = configured
        return if (currentConfigured == null) {
            val get = getConfiguration ?: throw IllegalStateException()
            get().also { this.configured = it }
        } else {
            currentConfigured
        }
    }

    override operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): Delegate<T> {
        return synchronized(this) {
            val functionName = property.name
            println("[${this::class.java.simpleName} provideDelegate] Creating function via property: $functionName")
            val delegate = Delegate {
                provide(property)
            }
            this.getConfiguration = { provide(property) }
            delegate
        }
    }
}