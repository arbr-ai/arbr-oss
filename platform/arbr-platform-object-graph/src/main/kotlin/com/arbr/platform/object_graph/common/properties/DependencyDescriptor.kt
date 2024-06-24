package com.arbr.og.object_model.common.properties

import com.arbr.og.object_model.common.model.CompoundPropertyIdentifier

sealed class DependencyDescriptor(
    open val propertyIdentifier: CompoundPropertyIdentifier,
)

/**
 * The identified _inner_ value must not be null
 */
data class NonNullDependencyDescriptor(
    override val propertyIdentifier: CompoundPropertyIdentifier
): DependencyDescriptor(propertyIdentifier)

/**
 * The identified _outer_ value must exist
 */
data class PropertyValueExistenceDependencyDescriptor(
    override val propertyIdentifier: CompoundPropertyIdentifier
): DependencyDescriptor(propertyIdentifier)

/**
 * The identified _collection_ value must exist. This is not the same as being non-empty
 */
data class PropertyCollectionValueExistenceDependencyDescriptor(
    override val propertyIdentifier: CompoundPropertyIdentifier
): DependencyDescriptor(propertyIdentifier)

/**
 * The identified _reference_ value to another view must exist
 */
data class PropertyReferenceValueExistenceDependencyDescriptor(
    override val propertyIdentifier: CompoundPropertyIdentifier
): DependencyDescriptor(propertyIdentifier)