package com.arbr.types.homotopy.keys

sealed interface ResourceKey {
    /**
     * Class of the resource
     * We allow only plain classes with no type parameters to be resources, spiritually data classes
     */
    val clazz: Class<*>

    val key: String
        get() = clazz.canonicalName
}

