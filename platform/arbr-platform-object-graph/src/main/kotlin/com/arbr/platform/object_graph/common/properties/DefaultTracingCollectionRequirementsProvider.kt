package com.arbr.og.object_model.common.properties

import com.arbr.og.object_model.common.model.CompoundPropertyIdentifier
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min

@Suppress("IfThenToElvis")
class DefaultTracingCollectionRequirementsProvider : CollectionRequirementsProvider {
    private val requirementsMap = ConcurrentHashMap<String, CollectionRequirements>()

    private fun idKey(containerIdentifier: CompoundPropertyIdentifier) = containerIdentifier.toString()

    override fun requireSizeAtLeast(containerIdentifier: CompoundPropertyIdentifier, minSize: Int) {
        val key = idKey(containerIdentifier)
        requirementsMap.compute(key) { _, requirements ->
            if (requirements == null) {
                CollectionRequirements(containerIdentifier, minSize, null)
            } else {
                requirements.copy(
                    lowerSizeBound = max(minSize, requirements.lowerSizeBound ?: 0)
                )
            }
        }
    }

    override fun requireSizeAtMost(containerIdentifier: CompoundPropertyIdentifier, maxSize: Int) {
        val key = idKey(containerIdentifier)
        requirementsMap.compute(key) { _, requirements ->
            if (requirements == null) {
                CollectionRequirements(containerIdentifier, null, maxSize)
            } else {
                requirements.copy(
                    upperSizeBound = min(maxSize, requirements.upperSizeBound ?: Int.MAX_VALUE)
                )
            }
        }
    }

    override fun getCollectionRequirements(): List<CollectionRequirements> {
        return requirementsMap.values.toList()
    }
}
