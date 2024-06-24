/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables.records


import org.jooq.Field
import org.jooq.Record2
import org.jooq.Row2
import org.jooq.impl.TableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class UserAccountLinkAlphaCodeRecord private constructor() : TableRecordImpl<UserAccountLinkAlphaCodeRecord>(com.arbr.db.`public`.tables.UserAccountLinkAlphaCode.USER_ACCOUNT_LINK_ALPHA_CODE), Record2<Long?, String?> {

    open var userId: Long
        set(value): Unit = set(0, value)
        get(): Long = get(0) as Long

    open var code: String
        set(value): Unit = set(1, value)
        get(): String = get(1) as String

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row2<Long?, String?> = super.fieldsRow() as Row2<Long?, String?>
    override fun valuesRow(): Row2<Long?, String?> = super.valuesRow() as Row2<Long?, String?>
    override fun field1(): Field<Long?> = com.arbr.db.`public`.tables.UserAccountLinkAlphaCode.USER_ACCOUNT_LINK_ALPHA_CODE.USER_ID
    override fun field2(): Field<String?> = com.arbr.db.`public`.tables.UserAccountLinkAlphaCode.USER_ACCOUNT_LINK_ALPHA_CODE.CODE
    override fun component1(): Long = userId
    override fun component2(): String = code
    override fun value1(): Long = userId
    override fun value2(): String = code

    override fun value1(value: Long?): UserAccountLinkAlphaCodeRecord {
        set(0, value)
        return this
    }

    override fun value2(value: String?): UserAccountLinkAlphaCodeRecord {
        set(1, value)
        return this
    }

    override fun values(value1: Long?, value2: String?): UserAccountLinkAlphaCodeRecord {
        this.value1(value1)
        this.value2(value2)
        return this
    }

    /**
     * Create a detached, initialised UserAccountLinkAlphaCodeRecord
     */
    constructor(userId: Long, code: String): this() {
        this.userId = userId
        this.code = code
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised UserAccountLinkAlphaCodeRecord
     */
    constructor(value: com.arbr.db.`public`.tables.pojos.UserAccountLinkAlphaCode?): this() {
        if (value != null) {
            this.userId = value.userId
            this.code = value.code
            resetChangedOnNotNull()
        }
    }
}
