package com.arbr.gradle.field

import com.arbr.codegen.base.inputs.ArbrPrimitiveValueType
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property

sealed class ArbrObjectField(
    private val fieldName: String,
    objects: ObjectFactory,
) : Named {
    override fun getName(): String {
        return fieldName
    }

    @get:Suppress("unused")
    val type: Property<ArbrPrimitiveValueType> = objects.property(ArbrPrimitiveValueType::class.java)

    @get:Suppress("unused")
    val description: Property<String> = objects.property(String::class.java)
        .convention(DEFAULT_DESCRIPTION)

    @get:Suppress("unused")
    val required: Property<Boolean> = objects.property(Boolean::class.java)
        .convention(true)

    @get:Suppress("unused")
    open val codecClass: Property<String> = objects.property(String::class.java)

    companion object {
        private const val DEFAULT_DESCRIPTION = ""
    }
}
