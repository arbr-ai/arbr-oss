package com.arbr.og.object_model.common.model.collections

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import java.util.*

interface OneToManyResourcePartialMap<T> {
    val uuid: String
    val items: ImmutableLinkedMap<String, T>?

    fun copy(
        items: ImmutableLinkedMap<String, T>? = null,
    ): OneToManyResourcePartialMap<T> {
        val thisOTM = this
        return if (items != null) {
            object : OneToManyResourcePartialMap<T> {
                override val uuid: String
                    get() = thisOTM.uuid
                override val items: ImmutableLinkedMap<String, T>
                    get() = items

            }
        } else {
            // Construct a new object in case this instance is immutable
            object : OneToManyResourcePartialMap<T> {
                override val uuid: String = thisOTM.uuid
                override val items: ImmutableLinkedMap<String, T>? = thisOTM.items
            }
        }
    }

    companion object {
        fun <T> new(
            items: ImmutableLinkedMap<String, T>? = null
        ): OneToManyResourcePartialMap<T> {
            return OneToManyResourcePartialMapImpl(
                UUID.randomUUID().toString(),
                items,
            )
        }

    }
}
