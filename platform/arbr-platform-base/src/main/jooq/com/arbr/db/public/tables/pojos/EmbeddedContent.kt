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
data class EmbeddedContent(
    val id: Long? = null,
    val creationTimestamp: Long,
    val resourceId: Long,
    val vectorId: String,
    val schemaId: String,
    val kind: String,
    val embeddingContent: String,
    val metadata: JSONB? = null
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: EmbeddedContent = other as EmbeddedContent
        if (this.id == null) {
            if (o.id != null)
                return false
        }
        else if (this.id != o.id)
            return false
        if (this.creationTimestamp != o.creationTimestamp)
            return false
        if (this.resourceId != o.resourceId)
            return false
        if (this.vectorId != o.vectorId)
            return false
        if (this.schemaId != o.schemaId)
            return false
        if (this.kind != o.kind)
            return false
        if (this.embeddingContent != o.embeddingContent)
            return false
        if (this.metadata == null) {
            if (o.metadata != null)
                return false
        }
        else if (this.metadata != o.metadata)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.id == null) 0 else this.id.hashCode())
        result = prime * result + this.creationTimestamp.hashCode()
        result = prime * result + this.resourceId.hashCode()
        result = prime * result + this.vectorId.hashCode()
        result = prime * result + this.schemaId.hashCode()
        result = prime * result + this.kind.hashCode()
        result = prime * result + this.embeddingContent.hashCode()
        result = prime * result + (if (this.metadata == null) 0 else this.metadata.hashCode())
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("EmbeddedContent (")

        sb.append(id)
        sb.append(", ").append(creationTimestamp)
        sb.append(", ").append(resourceId)
        sb.append(", ").append(vectorId)
        sb.append(", ").append(schemaId)
        sb.append(", ").append(kind)
        sb.append(", ").append(embeddingContent)
        sb.append(", ").append(metadata)

        sb.append(")")
        return sb.toString()
    }
}
