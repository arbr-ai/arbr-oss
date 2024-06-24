package com.arbr.gradle

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class ArbrObjectReference @Inject constructor(
    val name: String,
    objects: ObjectFactory,
) {
    @get:Suppress("unused")
    val description: Property<String> = objects.property(String::class.java)
        .convention(DEFAULT_DESCRIPTION)

    /**
     * The target data type for the reference.
     */
    @get:Suppress("unused")
    val targetDataType: Property<ArbrDataType> = objects.property(ArbrDataType::class.java)

    companion object {
        private const val DEFAULT_DESCRIPTION = ""
    }
}