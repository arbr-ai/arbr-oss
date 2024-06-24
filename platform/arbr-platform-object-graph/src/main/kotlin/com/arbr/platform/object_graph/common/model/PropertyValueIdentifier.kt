package com.arbr.og.object_model.common.model

/**
 * Identifier of a particular "frozen" property value, i.e. a stream element.
 */
interface PropertyValueIdentifier: PropertyDetachedIdentifier, ResourceIdentifier {
    val propertyValueUuid: String
}
