package com.arbr.platform.object_graph.store

import com.arbr.object_model.core.types.naming.NamedForeignKey
import com.arbr.og.object_model.common.model.ProposedForeignKeyFluxDelegator
import com.arbr.platform.object_graph.common.ForeignKeyChildSingleParentUpdate
import com.arbr.platform.object_graph.impl.ObjectModelResource
import reactor.core.publisher.Flux
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull


class DefaultKVStore<ForeignKey: NamedForeignKey>(
    backingStore: KVStoreBackingObjectStore<ForeignKey, ObjectModelResource<*, *, ForeignKey>>,
) : BaseKVStore<ForeignKey, ObjectModelResource<*, *, ForeignKey>>(backingStore) {

    private val latestValueFluxMap: ConcurrentHashMap<ForeignKey, ProposedForeignKeyFluxDelegator<ForeignKey>> =
        ConcurrentHashMap()

    override fun handleNewElement(newElement: ObjectModelResource<*, *, ForeignKey>) {
        newElement.getForeignKeys().entries.forEach { (foreignKey, pvs) ->
            latestValueFluxMap.compute(foreignKey) { _, combiner ->
                (combiner ?: ProposedForeignKeyFluxDelegator<ForeignKey>())
                    .also {
                        // TODO: I think this should be getLatestValue to match Delegator calls, but this is critical
                        // enough to need verification. Since this is an initial value, there shouldn't be real cases
                        // where a parent UUID is proposed and not accepted
                        it.add(newElement, pvs.getLatestAcceptedValue(), pvs)
                    }
            }
        }
        newElement.getChildren().entries.forEach { (foreignKey, _) ->
            latestValueFluxMap.compute(foreignKey) { _, combiner ->
                (combiner ?: ProposedForeignKeyFluxDelegator<ForeignKey>())
                    .also {
                        @Suppress("ReactiveStreamsUnusedPublisher")
                        it.getFlux(newElement.uuid)
                    }
            }
        }
    }

    /**
     * Flux of pairs of child resource to optional parent ref
     * TODO: Typed variants
     */
    private fun foreignKeyIndex(
        foreignKey: ForeignKey,
        parentUuid: String
    ): Flux<ProposedForeignKeyFluxDelegator.Update<ForeignKey>> {
        return latestValueFluxMap.compute(foreignKey) { _, combiner ->
            combiner ?: ProposedForeignKeyFluxDelegator()
        }!!.getFlux(parentUuid)
    }

    override fun childResourceParentUpdates(
        foreignKey: ForeignKey,
        parentUuid: String
    ): Flux<ForeignKeyChildSingleParentUpdate<ForeignKey, ObjectModelResource<*, *, ForeignKey>>> {
        return foreignKeyIndex(foreignKey, parentUuid).map {
            val nullableSetParentUuid = it.optionalParentRef.getOrNull()?.uuid
            ForeignKeyChildSingleParentUpdate(
                foreignKey,
                parentUuid,
                it.isAccepted,
                it.childResource,
                nullableSetParentUuid == parentUuid,
            )
        }
    }

    companion object {
        private const val LOADER_CLASS_ENV_VAR = "ARBR_KV_STORE_BACKING_OBJECT_STORE_LOADER_CLASS"
        private const val LOADER_CLASS_PROPERTY = "com.arbr.kv_store.loader"

        private val defaultLoaderClass = ConcurrentHashMapBackingStoreLoader::class.java

        private fun loaderClass(): Class<out KVStoreBackingObjectStoreLoader> {
            val fqClassName = System.getenv(LOADER_CLASS_ENV_VAR)
                ?: System.getProperty(LOADER_CLASS_PROPERTY)

            val loaderClass = if (fqClassName == null) {
                defaultLoaderClass
            } else {
                @Suppress("UNCHECKED_CAST")
                defaultLoaderClass.classLoader.loadClass(fqClassName) as Class<out KVStoreBackingObjectStoreLoader>
            }

            return loaderClass
        }

        private fun loader(): KVStoreBackingObjectStoreLoader {
            val loaderClass = loaderClass()
            val constructor = loaderClass.getConstructor()
            return constructor.newInstance()
        }

        private fun <ForeignKey: NamedForeignKey> backingStore(): KVStoreBackingObjectStore<ForeignKey, ObjectModelResource<*, *, ForeignKey>> {
            val loader = loader()
            return loader.load()
        }

        private val kvStoreMap = ConcurrentHashMap<Class<*>, DefaultKVStore<*>>()

        @Suppress("UNCHECKED_CAST")
        fun <ForeignKey: NamedForeignKey> loadConfiguredKvStore(
            keyClass: Class<ForeignKey>,
        ): DefaultKVStore<ForeignKey> {
            return kvStoreMap.computeIfAbsent(keyClass) {
                val backingStore = backingStore<ForeignKey>()
                DefaultKVStore(backingStore)
            } as DefaultKVStore<ForeignKey>
        }

        inline fun <reified ForeignKey: NamedForeignKey> loadConfiguredKvStore(): DefaultKVStore<ForeignKey> {
            val keyClass = ForeignKey::class.java
            return loadConfiguredKvStore(keyClass)
        }
    }
}

