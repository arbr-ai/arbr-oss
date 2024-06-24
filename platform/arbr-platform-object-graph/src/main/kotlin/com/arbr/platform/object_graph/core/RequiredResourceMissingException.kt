package com.arbr.platform.object_graph.core

import com.arbr.og.object_model.common.model.ProposedValue
import com.arbr.og.object_model.common.model.ProposedValueReadStream
import com.arbr.og.object_model.common.model.PropertyIdentifier
import com.arbr.platform.object_graph.concurrency.LockLevel
import java.time.Instant

sealed class RequiredValueMissingBaseException(
    private val customMessage: String? = null
) : Exception() {

    abstract val shortName: String

    abstract val dependencyIdentifier: PropertyIdentifier?

    override val message: String
        get() = customMessage ?: shortName
}

class RequiredResourceMissingException(
    val obj: ProposedValueReadStream<*>,
    val objClass: Class<*>,
    val customMessage: String? = null
) : RequiredValueMissingBaseException(
    customMessage ?: objClass.canonicalName
) {

    override val shortName: String = "${obj.identifier.resourceKey.name}.${obj.identifier.propertyKey.name}"

    override val dependencyIdentifier: PropertyIdentifier
        get() = obj.identifier

}

class RequiredProposedValueMissingException(
    val obj: ProposedValue<*>,
    val objClass: Class<*>,
    val customMessage: String? = null
) : RequiredValueMissingBaseException(customMessage) {

    override val shortName: String = "${obj.identifier.resourceKey.name}[${obj.identifier.resourceUuid}].${obj.identifier.propertyKey.name}"

    override val dependencyIdentifier: PropertyIdentifier
        get() = obj.identifier

}

class RequiredValueMissingException(val objClass: Class<*>, val customMessage: String? = null) :
    RequiredValueMissingBaseException(customMessage) {
    override val shortName: String
        get() = "base:" + objClass.simpleName

    override val dependencyIdentifier: PropertyIdentifier?
        get() = null
}

class RequiredLockAcquireException(
    val kind: String,
    val resourceUuid: String,
    val resourceTypeDisplayName: String,
    val ownerOperationKey: String,
    val lockLevelRequested: LockLevel,
    val conflictingHolderKey: String,
    val conflictingHolderLockLevel: LockLevel,
    val conflictingHolderCreationTimestampMs: Long,
): RequiredValueMissingBaseException() {
    override val shortName: String
        get() {
            val age = Instant.now().toEpochMilli() - conflictingHolderCreationTimestampMs
            return "lock:$kind:$resourceTypeDisplayName:${conflictingHolderKey}:age=${age}ms"
        }

    override val dependencyIdentifier: PropertyIdentifier?
        get() = null

    override val message: String = shortName
}

class RequiredLockedResourceRenderException(
    val resourceUuid: String,
    val resourceDisplayName: String,
): RequiredValueMissingBaseException() {
    override val shortName: String
        get() = "render:$resourceDisplayName[$resourceUuid]"

    override val dependencyIdentifier: PropertyIdentifier?
        get() = null

    override val message: String = shortName
}

class PostConditionFailedException(override val message: String? = null, override val cause: Throwable? = null): Exception(message, cause)

/**
 * Outputs already produced.
 */
class OperationCompleteException : Exception()
