package com.arbr.og.object_model.common.functions.spec.base

import com.arbr.object_model.core.types.ResourceView

interface GraphObjectQueryPivotSpec<RV : ResourceView<*>> {
    /**
     * Pivot type of the querying - for example, can be the resource "listened" to such that creating a resource of
     * this type triggers events leading to a querying.
     *
     * For now, we support only one pivot, but could support multiple in the future for binary functions and even
     * higher arity
     */
    val pivotClass: Class<RV>
}
