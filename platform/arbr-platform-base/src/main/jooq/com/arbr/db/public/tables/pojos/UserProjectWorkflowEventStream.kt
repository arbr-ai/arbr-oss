/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables.pojos


import java.io.Serializable


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class UserProjectWorkflowEventStream(
    val id: Long? = null,
    val workflowId: Long,
    val topicName: String,
    val stateHash: String,
    val ordinalOffset: Long,
    val eventTimestamp: Long
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: UserProjectWorkflowEventStream = other as UserProjectWorkflowEventStream
        if (this.id == null) {
            if (o.id != null)
                return false
        }
        else if (this.id != o.id)
            return false
        if (this.workflowId != o.workflowId)
            return false
        if (this.topicName != o.topicName)
            return false
        if (this.stateHash != o.stateHash)
            return false
        if (this.ordinalOffset != o.ordinalOffset)
            return false
        if (this.eventTimestamp != o.eventTimestamp)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.id == null) 0 else this.id.hashCode())
        result = prime * result + this.workflowId.hashCode()
        result = prime * result + this.topicName.hashCode()
        result = prime * result + this.stateHash.hashCode()
        result = prime * result + this.ordinalOffset.hashCode()
        result = prime * result + this.eventTimestamp.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("UserProjectWorkflowEventStream (")

        sb.append(id)
        sb.append(", ").append(workflowId)
        sb.append(", ").append(topicName)
        sb.append(", ").append(stateHash)
        sb.append(", ").append(ordinalOffset)
        sb.append(", ").append(eventTimestamp)

        sb.append(")")
        return sb.toString()
    }
}
