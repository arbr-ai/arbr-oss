package com.arbr.og.object_model.common.requirements

import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.og.object_model.common.model.ProposedForeignKeyCollectionStream
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.og.object_model.common.properties.NonNullRequirementsProvider
import com.arbr.platform.object_graph.core.PropertyValueLocker
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.ObjectRef
import com.arbr.platform.object_graph.impl.Partial

interface RequirementsProvider: AccessContextWriteRequirementsProvider, NonNullRequirementsProvider {
    val propertyValueLocker: ThreadLocal<PropertyValueLocker>

    fun <U : Any> requireTyped(
        uStream: ProposedValueReadStream<U>,
        uClass: Class<U>,
        message: String?,
    ): U

    fun <
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireChildrenTyped(
        proposedForeignKeyCollectionStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
        tClass: Class<T>,
        message: String?,
    ): ImmutableLinkedMap<String, T>

    fun <
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireThatValueTyped(
        proposedForeignKeyCollectionStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
        tClass: Class<T>,
        condition: (ImmutableLinkedMap<String, T>) -> Boolean,
    ): ImmutableLinkedMap<String, T>

    fun <
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireAttachedTyped(
        uStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
        tClass: Class<ObjectRef<out T, P, ForeignKey>>,
        message: String?,
    ): T

    fun <
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireAttachedOrElseCompleteTyped(
        uStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
        tClass: Class<ObjectRef<out T, P, ForeignKey>>,
        message: String?,
    ): T

    /**
     * PVS Latest
     */

    fun <U : Any> requireThatLatestTyped(
        uStream: ProposedValueReadStream<U>,
        uClass: Class<U>,
        condition: (U) -> Boolean
    ): U

    fun <U : Any> requireLatestTyped(
        uStream: ProposedValueReadStream<U>,
        uClass: Class<U>,
        message: String?,
    ): U

    fun <
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireLatestAttachedTyped(
        uStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
        uClass: Class<ObjectRef<out T, P, ForeignKey>>,
        message: String?,
    ): T

    fun <
            T : ObjectModelResource<T, P, ForeignKey>,
            P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireLatestChildrenTyped(
        uStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
        tClass: Class<T>,
        message: String?,
    ): ImmutableLinkedMap<String, T>

    fun latestContext(): AccessContextRequirementsProvider {
        return LatestContextRequirementsProviderImpl(this)
    }

    fun acceptContext(): AccessContextRequirementsProvider {
        return AcceptContextRequirementsProviderImpl(this)
    }

    private class LatestContextRequirementsProviderImpl(
        private val requirementsProvider: RequirementsProvider,
    ) : AccessContextRequirementsProvider {
        override fun <U : Any> require(
            uStream: ProposedValueReadStream<U>,
            uClass: Class<U>
        ): U {
            return requirementsProvider.requireLatestTyped(
                uStream,
                uClass,
                null,
            )
        }

        override fun <
                T : ObjectModelResource<T, P, ForeignKey>,
                P : Partial<T, P, ForeignKey>,
                ForeignKey: NamedForeignKey,
                > requireChildren(
            proposedForeignKeyCollectionStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
            tClass: Class<T>
        ): ImmutableLinkedMap<String, T> {
            return requirementsProvider.requireLatestChildrenTyped(
                proposedForeignKeyCollectionStream,
                tClass,
                null,
            )
        }

        override fun <
                T : ObjectModelResource<T, P, ForeignKey>,
                P : Partial<T, P, ForeignKey>,
                ForeignKey: NamedForeignKey,
                > requireAttached(
            uStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
            tClass: Class<ObjectRef<out T, P, ForeignKey>>
        ): T {
            return requirementsProvider.requireLatestAttachedTyped(
                uStream,
                tClass,
                null,
            )
        }
    }

    private class AcceptContextRequirementsProviderImpl(
        private val requirementsProvider: RequirementsProvider,
    ) : AccessContextRequirementsProvider {
        override fun <U : Any> require(
            uStream: ProposedValueReadStream<U>,
            uClass: Class<U>
        ): U {
            return requirementsProvider.requireTyped(
                uStream,
                uClass,
                null,
            )
        }

        override fun <T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey,> requireChildren(
            proposedForeignKeyCollectionStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
            tClass: Class<T>
        ): ImmutableLinkedMap<String, T> {
            return requirementsProvider.requireChildrenTyped(
                proposedForeignKeyCollectionStream,
                tClass,
                null,
            )
        }

        override fun <T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey: NamedForeignKey,> requireAttached(
            uStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
            tClass: Class<ObjectRef<out T, P, ForeignKey>>
        ): T {
            return requirementsProvider.requireAttachedTyped(
                uStream,
                tClass,
                null,
            )
        }
    }
}

