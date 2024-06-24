/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.pojos


import java.io.Serializable

import org.jooq.JSONB


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class GithubCommitRecord(
    val commitInfoSha: String,
    val pullRequestId: Long,
    val ordinal: Int,
    val tags: JSONB? = null
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: GithubCommitRecord = other as GithubCommitRecord
        if (this.commitInfoSha != o.commitInfoSha)
            return false
        if (this.pullRequestId != o.pullRequestId)
            return false
        if (this.ordinal != o.ordinal)
            return false
        if (this.tags == null) {
            if (o.tags != null)
                return false
        }
        else if (this.tags != o.tags)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + this.commitInfoSha.hashCode()
        result = prime * result + this.pullRequestId.hashCode()
        result = prime * result + this.ordinal.hashCode()
        result = prime * result + (if (this.tags == null) 0 else this.tags.hashCode())
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("GithubCommitRecord (")

        sb.append(commitInfoSha)
        sb.append(", ").append(pullRequestId)
        sb.append(", ").append(ordinal)
        sb.append(", ").append(tags)

        sb.append(")")
        return sb.toString()
    }
}