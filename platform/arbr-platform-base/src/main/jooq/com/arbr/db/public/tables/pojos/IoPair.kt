/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables.pojos


import java.io.Serializable


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class IoPair(
    val id: Long? = null,
    val creationTimestamp: Long,
    val inputResourceId: Long,
    val outputResourceId: Long
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: IoPair = other as IoPair
        if (this.id == null) {
            if (o.id != null)
                return false
        }
        else if (this.id != o.id)
            return false
        if (this.creationTimestamp != o.creationTimestamp)
            return false
        if (this.inputResourceId != o.inputResourceId)
            return false
        if (this.outputResourceId != o.outputResourceId)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + (if (this.id == null) 0 else this.id.hashCode())
        result = prime * result + this.creationTimestamp.hashCode()
        result = prime * result + this.inputResourceId.hashCode()
        result = prime * result + this.outputResourceId.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("IoPair (")

        sb.append(id)
        sb.append(", ").append(creationTimestamp)
        sb.append(", ").append(inputResourceId)
        sb.append(", ").append(outputResourceId)

        sb.append(")")
        return sb.toString()
    }
}