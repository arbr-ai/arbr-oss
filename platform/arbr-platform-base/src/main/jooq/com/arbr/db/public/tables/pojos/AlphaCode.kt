/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables.pojos


import java.io.Serializable


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
data class AlphaCode(
    val code: String
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false
        val o: AlphaCode = other as AlphaCode
        if (this.code != o.code)
            return false
        return true
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + this.code.hashCode()
        return result
    }

    override fun toString(): String {
        val sb = StringBuilder("AlphaCode (")

        sb.append(code)

        sb.append(")")
        return sb.toString()
    }
}