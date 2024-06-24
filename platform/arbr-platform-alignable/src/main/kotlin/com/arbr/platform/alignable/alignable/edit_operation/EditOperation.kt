package com.arbr.platform.alignable.alignable.edit_operation

interface EditOperation<Q> {

    fun applyTo(state: Q): Q
}