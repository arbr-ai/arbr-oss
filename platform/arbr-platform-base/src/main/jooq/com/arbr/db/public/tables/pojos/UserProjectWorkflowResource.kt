/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables.pojos


import java.io.Serializable

import org.jooq.JSONB


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class UserProjectWorkflowResource(
    val id: Long? = null,
    val objectModelUuid: String,
    val creationTimestamp: Long,
    val updatedTimestamp: Long,
    val workflowId: Long,
    val resourceType: String,
    val parentResourceId: Long? = null,
    val resourceData: JSONB,
    val ordinal: Int,
    val isValid: Boolean
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: UserProjectWorkflowResource = other as UserProjectWorkflowResource
        if (this.id == null) {
            if (o.id != null)
                return false
        }
        else if (this.id != o.id)
            return false
        if (this.objectModelUuid != o.objectModelUuid)
            return false
        if (this.creationTimestamp != o.creationTimestamp)
            return false
        if (this.updatedTimestamp != o.updatedTimestamp)
            return false
        if (this.workflowId != o.workflowId)
            return false
        if (this.resourceType != o.resourceType)
            return false
        if (this.parentResourceId == null) {
            if (o.parentResourceId != null)
                return false
        }
        else if (this.parentResourceId != o.parentResourceId)
            return false
        if (this.resourceData != o.resourceData)
            return false
        if (this.ordinal != o.ordinal)
            return false
        if (this.isValid != o.isValid)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.id == null) 0 else this.id.hashCode())
        result = prime * result + this.objectModelUuid.hashCode()
        result = prime * result + this.creationTimestamp.hashCode()
        result = prime * result + this.updatedTimestamp.hashCode()
        result = prime * result + this.workflowId.hashCode()
        result = prime * result + this.resourceType.hashCode()
        result = prime * result + (if (this.parentResourceId == null) 0 else this.parentResourceId.hashCode())
        result = prime * result + this.resourceData.hashCode()
        result = prime * result + this.ordinal.hashCode()
        result = prime * result + this.isValid.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("UserProjectWorkflowResource (")

        sb.append(id)
        sb.append(", ").append(objectModelUuid)
        sb.append(", ").append(creationTimestamp)
        sb.append(", ").append(updatedTimestamp)
        sb.append(", ").append(workflowId)
        sb.append(", ").append(resourceType)
        sb.append(", ").append(parentResourceId)
        sb.append(", ").append(resourceData)
        sb.append(", ").append(ordinal)
        sb.append(", ").append(isValid)

        sb.append(")")
        return sb.toString()
    }
}
