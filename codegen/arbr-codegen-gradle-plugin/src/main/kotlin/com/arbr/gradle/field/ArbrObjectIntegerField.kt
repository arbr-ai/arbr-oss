package com.arbr.gradle.field

import com.arbr.codegen.base.inputs.ArbrPrimitiveValueType
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

abstract class ArbrObjectIntegerField @Inject constructor(
    name: String,
    objects: ObjectFactory,
): ArbrObjectField(name, objects) {
    override val codecClass: Property<String> = super
        .codecClass
        .convention(DEFAULT_CODEC_CLASS)

    init {
        type.set(ArbrPrimitiveValueType.INTEGER)
    }

    companion object {
        const val DEFAULT_CODEC_CLASS = "com.arbr.og.encoder.ObjectIntegerFieldCodec"
    }
}