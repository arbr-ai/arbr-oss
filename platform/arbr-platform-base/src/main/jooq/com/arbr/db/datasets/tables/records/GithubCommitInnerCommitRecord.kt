/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.records


import com.arbr.db.datasets.tables.GithubCommitInnerCommit

import org.jooq.Field
import org.jooq.Record2
import org.jooq.Row2
import org.jooq.impl.TableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class GithubCommitInnerCommitRecord private constructor() : TableRecordImpl<GithubCommitInnerCommitRecord>(GithubCommitInnerCommit.GITHUB_COMMIT_INNER_COMMIT), Record2<String?, String?> {

    open var message: String
        set(value): Unit = set(0, value)
        get(): String = get(0) as String

    open var commitInfoSha: String
        set(value): Unit = set(1, value)
        get(): String = get(1) as String

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row2<String?, String?> = super.fieldsRow() as Row2<String?, String?>
    override fun valuesRow(): Row2<String?, String?> = super.valuesRow() as Row2<String?, String?>
    override fun field1(): Field<String?> = GithubCommitInnerCommit.GITHUB_COMMIT_INNER_COMMIT.MESSAGE
    override fun field2(): Field<String?> = GithubCommitInnerCommit.GITHUB_COMMIT_INNER_COMMIT.COMMIT_INFO_SHA
    override fun component1(): String = message
    override fun component2(): String = commitInfoSha
    override fun value1(): String = message
    override fun value2(): String = commitInfoSha

    override fun value1(value: String?): GithubCommitInnerCommitRecord {
        set(0, value)
        return this
    }

    override fun value2(value: String?): GithubCommitInnerCommitRecord {
        set(1, value)
        return this
    }

    override fun values(value1: String?, value2: String?): GithubCommitInnerCommitRecord {
        this.value1(value1)
        this.value2(value2)
        return this
    }

    /**
     * Create a detached, initialised GithubCommitInnerCommitRecord
     */
    constructor(message: String, commitInfoSha: String): this() {
        this.message = message
        this.commitInfoSha = commitInfoSha
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised GithubCommitInnerCommitRecord
     */
    constructor(value: com.arbr.db.datasets.tables.pojos.GithubCommitInnerCommit?): this() {
        if (value != null) {
            this.message = value.message
            this.commitInfoSha = value.commitInfoSha
            resetChangedOnNotNull()
        }
    }
}
