package com.arbr.platform.object_graph.common

class ObjectValueEquatable<ValueType>(
    val objectValue: ObjectModel.ObjectValue<ValueType, *, *, *>,
) {
    override fun equals(other: Any?): Boolean {
        return if (other != null && other is ObjectValueEquatable<*>) {
            other.objectValue.typeName == objectValue.typeName
                    && other.objectValue.value == objectValue.value
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        return objectValue.typeName.hashCode().xor(objectValue.value.hashCode())
    }
}