package com.arbr.platform.object_graph.impl

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

sealed interface ObjectRef<T: ObjectModelResource<T, P, ForeignKey>, P: Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey> {
    val uuid: String?

    @JsonIgnore
    fun resource(): T?

    @JsonIgnore
    fun resourceErased(): ObjectModelResource<T, P, ForeignKey>?

    @JsonIgnore
    fun partialRef(): PartialRef<T, P>

    class OfUuid<T: ObjectModelResource<T, P, ForeignKey>, P: Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey>(
        override val uuid: String?,
    ): ObjectRef<T, P, ForeignKey> {
        private var _resource: T? = null

        private fun getFromGlobalKvStore(): ObjectModelResource<*, *, *>? {
            return uuid?.let { id ->
                // ObjectModel.kvStore[id]
                TODO()
            }
        }

        @Suppress("UNCHECKED_CAST")
        override fun resource(): T? {
            if (uuid == null) {
                return null
            }

            val resource = _resource
            if (resource != null) {
                return resource
            }

            val stored = getFromGlobalKvStore()
                ?: throw Exception("Expected value present in global store for $uuid")

            _resource = stored as T
            return stored
        }

        override fun resourceErased(): ObjectModelResource<T, P, ForeignKey>? {
            return resource()
        }

        override fun partialRef(): PartialRef<T, P> {
            return PartialRef(uuid)
        }

        override fun toString(): String {
            return "ObjectRef[uuid=${uuid}]"
        }
    }

    class OfResource<T: ObjectModelResource<T, P, ForeignKey>, P: Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey>(
        private val resource: T
    ): ObjectRef<T, P, ForeignKey> {
        override val uuid: String
            get() = resource.uuid

        override fun resource(): T {
            return resource
        }

        override fun resourceErased(): ObjectModelResource<T, P, ForeignKey>? {
            return resource()
        }

        override fun partialRef(): PartialRef<T, P> {
            return PartialRef(uuid)
        }

        override fun toString(): String {
            return "ObjectRef[resource.uuid=${resource.uuid}]"
        }
    }

    fun nullified(): ObjectRef<T, P, ForeignKey> {
        return OfUuid(null)
    }

    @JsonValue
    fun toJson() = mapOf("uuid" to uuid)
}
