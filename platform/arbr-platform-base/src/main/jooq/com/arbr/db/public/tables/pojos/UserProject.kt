/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables.pojos


import java.io.Serializable


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class UserProject(
    val id: Long? = null,
    val userId: Long,
    val creationTimestamp: Long,
    val fullName: String
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: UserProject = other as UserProject
        if (this.id == null) {
            if (o.id != null)
                return false
        }
        else if (this.id != o.id)
            return false
        if (this.userId != o.userId)
            return false
        if (this.creationTimestamp != o.creationTimestamp)
            return false
        if (this.fullName != o.fullName)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.id == null) 0 else this.id.hashCode())
        result = prime * result + this.userId.hashCode()
        result = prime * result + this.creationTimestamp.hashCode()
        result = prime * result + this.fullName.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("UserProject (")

        sb.append(id)
        sb.append(", ").append(userId)
        sb.append(", ").append(creationTimestamp)
        sb.append(", ").append(fullName)

        sb.append(")")
        return sb.toString()
    }
}
