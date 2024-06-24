/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.pojos


import java.io.Serializable


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class GithubFileReference(
    val id: Long? = null,
    val repoFullName: String,
    val commitInfoSha: String,
    val filename: String,
    val uri: String
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: GithubFileReference = other as GithubFileReference
        if (this.id == null) {
            if (o.id != null)
                return false
        }
        else if (this.id != o.id)
            return false
        if (this.repoFullName != o.repoFullName)
            return false
        if (this.commitInfoSha != o.commitInfoSha)
            return false
        if (this.filename != o.filename)
            return false
        if (this.uri != o.uri)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.id == null) 0 else this.id.hashCode())
        result = prime * result + this.repoFullName.hashCode()
        result = prime * result + this.commitInfoSha.hashCode()
        result = prime * result + this.filename.hashCode()
        result = prime * result + this.uri.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("GithubFileReference (")

        sb.append(id)
        sb.append(", ").append(repoFullName)
        sb.append(", ").append(commitInfoSha)
        sb.append(", ").append(filename)
        sb.append(", ").append(uri)

        sb.append(")")
        return sb.toString()
    }
}
