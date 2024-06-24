package com.arbr.og.object_model.common.model.collections

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap

internal data class OneToManyResourcePartialMapImpl<T>(
    override val uuid: String,
    override val items: ImmutableLinkedMap<String, T>? = null,
) : OneToManyResourcePartialMap<T>
