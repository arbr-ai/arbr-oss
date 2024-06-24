package com.arbr.og.object_model.common.requirements

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.og.object_model.common.model.ProposedForeignKeyCollectionStream
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.og.object_model.common.values.SourcedValue
import com.arbr.og.object_model.common.values.SourcedValueImpl
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.platform.object_graph.concurrency.LockedResourceRenderException
import com.arbr.platform.object_graph.core.OperationCompleteException
import com.arbr.platform.object_graph.core.PropertyValueLocker
import com.arbr.platform.object_graph.core.RequiredResourceMissingException
import com.arbr.platform.object_graph.core.RequiredValueMissingException
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.ObjectRef
import com.arbr.platform.object_graph.impl.Partial
import com.arbr.util.invariants.Invariants

class DefaultRequirementsProvider: RequirementsProvider {
    /**
     * Relies on the fact that dependency calculation is synchronous
     */
    override val propertyValueLocker = ThreadLocal<PropertyValueLocker>()

    override fun <ValueType : Any, T : ObjectModel.ObjectValue<ValueType?, *, *, T>> getNonNullValue(
        objectValue: ObjectModel.ObjectValue<ValueType?, *, *, T>
    ): SourcedValue<ValueType> {
        if (objectValue.value == null) {
            throw RequiredValueMissingException(objectValue::class.java, null)
        }

        return SourcedValueImpl(
            objectValue.kind,
            objectValue.value!!,
            objectValue.typeName,
            objectValue.schema.requiredNonNull(),
            objectValue.generatorInfo,
        )
    }

    override fun <U : Any> requireTyped(
        uStream: ProposedValueReadStream<U>,
        uClass: Class<U>,
        message: String?,
    ): U {
        if (uStream.hasAnyUnresolvedProposals()) {
            throw LockedResourceRenderException(
                uStream.identifier.resourceUuid,
                "${uStream.identifier.resourceKey.name}:${uStream.identifier.propertyKey.name}",
            )
        }

        val result = uStream.getLatestAcceptedValue() ?: throw RequiredResourceMissingException(
            uStream,
            uClass,
            customMessage = message
        )

        val locker = propertyValueLocker.get()

        Invariants.check { require ->
            require(locker != null)
        }

        locker?.acquireRead(uStream.identifier)

        return result
    }

    override fun <T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey : NamedForeignKey> requireChildrenTyped(
        proposedForeignKeyCollectionStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
        tClass: Class<T>,
        message: String?
    ): ImmutableLinkedMap<String, T> {

        if (proposedForeignKeyCollectionStream.hasAnyUnresolvedProposals()) {
            throw LockedResourceRenderException(
                proposedForeignKeyCollectionStream.identifier.resourceUuid,
                "${proposedForeignKeyCollectionStream.identifier.resourceKey.name}:${proposedForeignKeyCollectionStream.identifier.propertyKey.name}",
            )
        }

        val acceptedValue =
            proposedForeignKeyCollectionStream.getLatestAcceptedValue() ?: throw RequiredResourceMissingException(
                proposedForeignKeyCollectionStream,
                tClass,
                customMessage = message
            )

        val result = acceptedValue.mapValuesNotNull {
            it.value.resource()
        }

        val locker = propertyValueLocker.get()

        Invariants.check { require ->
            require(locker != null)
        }

        locker?.acquireRead(proposedForeignKeyCollectionStream.identifier)

        return result
    }

    override fun <T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey : NamedForeignKey> requireThatValueTyped(
        proposedForeignKeyCollectionStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
        tClass: Class<T>,
        condition: (ImmutableLinkedMap<String, T>) -> Boolean
    ): ImmutableLinkedMap<String, T> {
        val unwrappedValue = requireChildrenTyped(proposedForeignKeyCollectionStream, tClass, null)

        if (!condition(unwrappedValue)) {
            throw RequiredResourceMissingException(
                proposedForeignKeyCollectionStream,
                tClass,
                customMessage = null,
            )
        }

        val locker = propertyValueLocker.get()

        Invariants.check { require ->
            require(locker != null)
        }

        locker?.acquireRead(proposedForeignKeyCollectionStream.identifier)

        return unwrappedValue
    }

    override fun <T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey : NamedForeignKey> requireAttachedTyped(
        uStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
        tClass: Class<ObjectRef<out T, P, ForeignKey>>,
        message: String?
    ): T {
        val ref = requireTyped(uStream, tClass, message)

        val result = ref.resource() ?: throw RequiredResourceMissingException(
            uStream,
            tClass,
            customMessage = message
        )

        val locker = propertyValueLocker.get()

        Invariants.check { require ->
            require(locker != null)
        }

        locker?.acquireRead(uStream.identifier)

        return result
    }

    override fun <T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey : NamedForeignKey> requireAttachedOrElseCompleteTyped(
        uStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
        tClass: Class<ObjectRef<out T, P, ForeignKey>>,
        message: String?
    ): T {
        val resourceRef = requireTyped(uStream, tClass, message) // Yet undecided

        val result = resourceRef.resource()
            ?: throw OperationCompleteException()

        val locker = propertyValueLocker.get()

        Invariants.check { require ->
            require(locker != null)
        }

        locker?.acquireRead(uStream.identifier)

        return result
    }

    /**
     * PVS Latest
     */

    override fun <U : Any> requireThatLatestTyped(
        uStream: ProposedValueReadStream<U>,
        uClass: Class<U>,
        condition: (U) -> Boolean
    ): U {
        val unwrappedValue = uStream.getLatestValue() ?: throw RequiredResourceMissingException(
            uStream,
            uClass,
            customMessage = null,
        )

        if (!condition(unwrappedValue)) {
            throw RequiredResourceMissingException(
                uStream,
                uClass,
                customMessage = null,
            )
        }

        return unwrappedValue
    }

    override fun <U : Any> requireLatestTyped(uStream: ProposedValueReadStream<U>, uClass: Class<U>, message: String?): U {
        return uStream.getLatestValue() ?: throw RequiredResourceMissingException(
            uStream,
            uClass,
            customMessage = message
        )
    }

    override fun <T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey : NamedForeignKey> requireLatestAttachedTyped(
        uStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
        uClass: Class<ObjectRef<out T, P, ForeignKey>>,
        message: String?
    ): T {
        return uStream.getLatestValue()?.resource() ?: throw RequiredResourceMissingException(
            uStream,
            uClass,
            customMessage = message
        )
    }

    override fun <T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey : NamedForeignKey> requireLatestChildrenTyped(
        uStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
        tClass: Class<T>,
        message: String?
    ): ImmutableLinkedMap<String, T> {
        return uStream.getLatestValue()?.mapValues { (_, objectRef) ->
            objectRef.resource() ?: throw RequiredResourceMissingException(
                uStream,
                tClass,
                customMessage = message
            )
        } ?: throw RequiredResourceMissingException(
            uStream,
            tClass,
            customMessage = message
        )
    }

    override fun <U : Any> requireWriteTo(uStream: ProposedValueReadStream<U>, uClass: Class<U>, uValue: U): U {
        TODO("Not yet implemented")
    }

    override fun <T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey : NamedForeignKey> requireWriteToChildren(
        proposedForeignKeyCollectionStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
        resourceClass: Class<T>,
        mapValue: ImmutableLinkedMap<String, ObjectRef<out T, P, ForeignKey>>
    ): ImmutableLinkedMap<String, T> {
        TODO("Not yet implemented")
    }

    override fun <T : ObjectModelResource<T, P, ForeignKey>, P : Partial<T, P, ForeignKey>, ForeignKey : NamedForeignKey> requireWriteToAttached(
        refStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
        refClass: Class<ObjectRef<out T, P, ForeignKey>>,
        refValue: ObjectRef<out T, P, ForeignKey>
    ): T {
        TODO("Not yet implemented")
    }

}
