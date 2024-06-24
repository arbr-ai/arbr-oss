package com.arbr.types.homotopy.keys

import kotlin.reflect.KProperty1

object PropertyKeys {
    fun ofProperties(properties: Collection<KProperty1<*, *>>): List<PropertyKey> {
        return properties.mapIndexed { index, kProperty1 ->
            val name = kProperty1.name
            PropertyKey.of(name, index)
        }
    }

}