package com.arbr.og.object_model.common.requirements

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.og.object_model.common.model.ProposedForeignKeyCollectionStream
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.og.object_model.common.model.collections.OneToManyResourceMap
import com.arbr.platform.data_structures_common.immutable.ImmutableLinkedMap
import com.arbr.platform.object_graph.core.RequiredResourceMissingException
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.impl.ObjectRef
import com.arbr.platform.object_graph.impl.Partial
import com.arbr.platform.object_graph.util.cls

/**
 * Wrapper providing reified versions of requirements methods
 * TODO: Deprecate when removing explicit require calls in processor functions
 */
abstract class RequirementsProviderWrapper(
    val innerProvider: RequirementsProvider,
) {

    /**
     * PVS Accepted
     */

    inline fun <reified U : Any> require(uStream: ProposedValueReadStream<U>, message: String? = null): U {
        return innerProvider.requireTyped(uStream, U::class.java, message)
    }

    inline fun <
            reified T : ObjectModelResource<T, P, ForeignKey>,
            reified P : Partial<T, P, ForeignKey>,
            reified ForeignKey: NamedForeignKey,
            > require(
        childMap: OneToManyResourceMap<T, P, ForeignKey>,
        message: String? = null
    ): ImmutableLinkedMap<String, T> {
        return innerProvider.requireChildrenTyped(childMap.items, T::class.java, message)
    }

    /**
     * TODO: Deprecate arbitrary conditions
     */
    inline fun <
            reified T : ObjectModelResource<T, P, ForeignKey>,
            reified P : Partial<T, P, ForeignKey>,
            reified ForeignKey: NamedForeignKey,
            > requireThatValue(
        childMap: OneToManyResourceMap<T, P, ForeignKey>,
        noinline condition: (ImmutableLinkedMap<String, T>) -> Boolean,
    ): ImmutableLinkedMap<String, T> {
        return innerProvider.requireThatValueTyped(childMap.items, T::class.java, condition)
    }

    inline fun <
            reified T : ObjectModelResource<T, P, ForeignKey>,
            reified P : Partial<T, P, ForeignKey>,
            reified ForeignKey: NamedForeignKey,
            > requireAttached(
        uStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
        message: String? = null
    ): T {
        return innerProvider.requireAttachedTyped(uStream, cls(), message)
    }

    inline fun <
            reified T : ObjectModelResource<T, P, ForeignKey>,
            reified P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireAttachedOrElseComplete(
        uStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
        message: String? = null
    ): T {
        return innerProvider.requireAttachedOrElseCompleteTyped(uStream, cls(), message)
    }

    /**
     * PVS Latest
     */

    /**
     * TODO: Deprecate arbitrary conditions
     */
    inline fun <reified U : Any> requireThatLatest(
        uStream: ProposedValueReadStream<U>,
        noinline condition: (U) -> Boolean
    ): U {
        return innerProvider.requireThatLatestTyped(uStream, cls(), condition)
    }

    fun requireLatestSatisfied(
        uStream: ProposedValueReadStream<Boolean>,
    ) {
        val satisfied = innerProvider.requireThatLatestTyped(uStream, Boolean::class.java) {
            it
        }

        if (!satisfied) {
            throw RequiredResourceMissingException(
                uStream,
                Boolean::class.java,
                customMessage = null,
            )
        }
    }

    inline fun <reified U : Any> requireLatest(uStream: ProposedValueReadStream<U>, message: String? = null): U {
        return innerProvider.requireLatestTyped(uStream, U::class.java, message)
    }

    inline fun <
            reified T : ObjectModelResource<T, P, ForeignKey>,
            reified P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireLatestAttached(
        uStream: ProposedValueReadStream<ObjectRef<out T, P, ForeignKey>>,
        message: String? = null
    ): T {
        return innerProvider.requireLatestAttachedTyped(uStream, cls(), message)
    }

    inline fun <
            reified T : ObjectModelResource<T, P, ForeignKey>,
            reified P : Partial<T, P, ForeignKey>,
            ForeignKey: NamedForeignKey,
            > requireLatestChildren(
        uStream: ProposedForeignKeyCollectionStream<T, P, ForeignKey>,
        message: String? = null
    ): ImmutableLinkedMap<String, T> {
        return innerProvider.requireLatestChildrenTyped(uStream, T::class.java, message)
    }

}