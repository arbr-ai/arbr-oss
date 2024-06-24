package com.arbr.gradle

import com.arbr.codegen.base.inputs.ArbrPlainDataType
import com.arbr.codegen.base.inputs.ArbrPlainObjectField
import com.arbr.codegen.base.inputs.ArbrPlainObjectReference
import com.arbr.gradle.field.ArbrObjectField

object ArbrDataTypePlainConverter {

    private fun convertObjectReference(
        arbrObjectReference: ArbrObjectReference
    ): ArbrPlainObjectReference {
        return ArbrPlainObjectReference(
            arbrObjectReference.name,
            arbrObjectReference.description.get(),
            arbrObjectReference.targetDataType.get().name,
        )
    }

    private fun convertObjectField(
        arbrObjectField: ArbrObjectField
    ): ArbrPlainObjectField {
        return ArbrPlainObjectField(
            arbrObjectField.name,
            arbrObjectField.description.get(),
            arbrObjectField.type.get(),
            arbrObjectField.required.get(),
            arbrObjectField.codecClass.get(),
        )
    }

    private fun includeObjectField(
        arbrObjectField: ArbrObjectField
    ): Boolean {
        // Filter UUID fields as a special case that is templated manually
//        return arbrObjectField.name != ArbrDataType.RESOURCE_ID_FIELD_NAME
        return true
    }

    fun convertDataType(
        arbrDataType: ArbrDataType,
    ): ArbrPlainDataType {
        // Self-parent, expected at the root
        val selfParent = arbrDataType.parentReference.targetDataType.get().name == arbrDataType.name

        val allRelations = arbrDataType.relations
        val parentReference = if (selfParent) {
            null
        } else {
            convertObjectReference(arbrDataType.parentReference)
        }

        val convertedFields = arbrDataType.fields
            .filter(this::includeObjectField)
            .map(this::convertObjectField)

        return ArbrPlainDataType(
            arbrDataType.name,
            arbrDataType.description.get(),
            arbrDataType.ordinal,
            convertedFields,
            parentReference,
            allRelations.map(this::convertObjectReference),
        )
    }

}