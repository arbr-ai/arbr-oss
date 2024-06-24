package com.arbr.engine.services.db.model

import com.arbr.db.binding.Vector1536
import java.io.Serializable

data class VectorQueryResult(
    val vectorId: String,
    val versionId: String,
    val namespace: String,
    val schemaId: String,
    val embeddingContent: String,
    val embedding: Vector1536,
    val distance: Double,
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: VectorQueryResult = other as VectorQueryResult
        if (this.vectorId != o.vectorId)
            return false
        if (this.versionId != o.versionId)
            return false
        if (this.namespace != o.namespace)
            return false
        if (this.schemaId != o.schemaId)
            return false
        if (this.embeddingContent != o.embeddingContent)
            return false
        if (this.embedding != o.embedding)
            return false
        return this.distance == o.distance
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + this.vectorId.hashCode()
        result = prime * result + this.versionId.hashCode()
        result = prime * result + this.namespace.hashCode()
        result = prime * result + this.schemaId.hashCode()
        result = prime * result + this.embeddingContent.hashCode()
        result = prime * result + this.embedding.hashCode()
        result = prime * result + this.distance.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("VectorEmbedding (")

        sb.append(vectorId)
        sb.append(", ").append(versionId)
        sb.append(", ").append(namespace)
        sb.append(", ").append(schemaId)
        sb.append(", ").append(embeddingContent)
        sb.append(", ").append(embedding)
        sb.append(", ").append(distance)

        sb.append(")")
        return sb.toString()
    }
}