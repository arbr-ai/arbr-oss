package com.arbr.platform.object_graph.common.model

/**
 * Identifier of a particular "frozen" property value, i.e. a stream element.
 */
interface PropertyValueIdentifier: PropertyDetachedIdentifier, ResourceIdentifier {
    val propertyValueUuid: String
}
