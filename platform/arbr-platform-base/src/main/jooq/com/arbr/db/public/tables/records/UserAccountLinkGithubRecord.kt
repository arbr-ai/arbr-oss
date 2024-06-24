/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables.records


import org.jooq.Field
import org.jooq.Record5
import org.jooq.Row5
import org.jooq.impl.TableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class UserAccountLinkGithubRecord private constructor() : TableRecordImpl<UserAccountLinkGithubRecord>(com.arbr.db.`public`.tables.UserAccountLinkGithub.USER_ACCOUNT_LINK_GITHUB), Record5<Long?, String?, String?, String?, Long?> {

    open var creationTimestamp: Long
        set(value): Unit = set(0, value)
        get(): Long = get(0) as Long

    open var githubId: String?
        set(value): Unit = set(1, value)
        get(): String? = get(1) as String?

    open var githubKey: String?
        set(value): Unit = set(2, value)
        get(): String? = get(2) as String?

    open var githubAppName: String?
        set(value): Unit = set(3, value)
        get(): String? = get(3) as String?

    open var userId: Long
        set(value): Unit = set(4, value)
        get(): Long = get(4) as Long

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row5<Long?, String?, String?, String?, Long?> = super.fieldsRow() as Row5<Long?, String?, String?, String?, Long?>
    override fun valuesRow(): Row5<Long?, String?, String?, String?, Long?> = super.valuesRow() as Row5<Long?, String?, String?, String?, Long?>
    override fun field1(): Field<Long?> = com.arbr.db.`public`.tables.UserAccountLinkGithub.USER_ACCOUNT_LINK_GITHUB.CREATION_TIMESTAMP
    override fun field2(): Field<String?> = com.arbr.db.`public`.tables.UserAccountLinkGithub.USER_ACCOUNT_LINK_GITHUB.GITHUB_ID
    override fun field3(): Field<String?> = com.arbr.db.`public`.tables.UserAccountLinkGithub.USER_ACCOUNT_LINK_GITHUB.GITHUB_KEY
    override fun field4(): Field<String?> = com.arbr.db.`public`.tables.UserAccountLinkGithub.USER_ACCOUNT_LINK_GITHUB.GITHUB_APP_NAME
    override fun field5(): Field<Long?> = com.arbr.db.`public`.tables.UserAccountLinkGithub.USER_ACCOUNT_LINK_GITHUB.USER_ID
    override fun component1(): Long = creationTimestamp
    override fun component2(): String? = githubId
    override fun component3(): String? = githubKey
    override fun component4(): String? = githubAppName
    override fun component5(): Long = userId
    override fun value1(): Long = creationTimestamp
    override fun value2(): String? = githubId
    override fun value3(): String? = githubKey
    override fun value4(): String? = githubAppName
    override fun value5(): Long = userId

    override fun value1(value: Long?): UserAccountLinkGithubRecord {
        set(0, value)
        return this
    }

    override fun value2(value: String?): UserAccountLinkGithubRecord {
        set(1, value)
        return this
    }

    override fun value3(value: String?): UserAccountLinkGithubRecord {
        set(2, value)
        return this
    }

    override fun value4(value: String?): UserAccountLinkGithubRecord {
        set(3, value)
        return this
    }

    override fun value5(value: Long?): UserAccountLinkGithubRecord {
        set(4, value)
        return this
    }

    override fun values(value1: Long?, value2: String?, value3: String?, value4: String?, value5: Long?): UserAccountLinkGithubRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        this.value5(value5)
        return this
    }

    /**
     * Create a detached, initialised UserAccountLinkGithubRecord
     */
    constructor(creationTimestamp: Long, githubId: String? = null, githubKey: String? = null, githubAppName: String? = null, userId: Long): this() {
        this.creationTimestamp = creationTimestamp
        this.githubId = githubId
        this.githubKey = githubKey
        this.githubAppName = githubAppName
        this.userId = userId
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised UserAccountLinkGithubRecord
     */
    constructor(value: com.arbr.db.`public`.tables.pojos.UserAccountLinkGithub?): this() {
        if (value != null) {
            this.creationTimestamp = value.creationTimestamp
            this.githubId = value.githubId
            this.githubKey = value.githubKey
            this.githubAppName = value.githubAppName
            this.userId = value.userId
            resetChangedOnNotNull()
        }
    }
}
