package com.arbr.object_model.core.types

/**
 * Read-only view
 */
interface ResourceView<R : GeneralResource> {
    val uuid: String
}
