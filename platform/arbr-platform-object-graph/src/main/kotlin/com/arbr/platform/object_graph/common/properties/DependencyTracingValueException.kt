package com.arbr.og.object_model.common.properties

import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.model.PropertyIdentifier

/**
 * Exception for dependency tracing phases when the innermost value of a property is requested unexpectedly.
 */
@Suppress("MemberVisibilityCanBePrivate")
class DependencyTracingValueException(
    val containerIdentifier: PropertyIdentifier,
): Exception() {
    override val message: String
        get() = "Unsatisfiable value access during dependency tracing. " +
                "Property ID: ${containerIdentifier.resourceKey.name}[${containerIdentifier.resourceUuid}].${containerIdentifier.propertyKey.name}"
}

class DependencyTracingNullValueException(
    val objectValue: ObjectModel.ObjectValue<*, *, *, *>,
): Exception() {
    override val message: String
        get() = "Unsatisfiable (null) value access during dependency tracing. " +
                "Property type: ${objectValue.objectType.typeName} ID: ${objectValue.id}"
}
