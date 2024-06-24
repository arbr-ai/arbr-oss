/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.records


import com.arbr.db.datasets.tables.GithubPullRequestRecord

import org.jooq.Field
import org.jooq.JSONB
import org.jooq.Record2
import org.jooq.Row2
import org.jooq.impl.TableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class GithubPullRequestRecordRecord private constructor() : TableRecordImpl<GithubPullRequestRecordRecord>(GithubPullRequestRecord.GITHUB_PULL_REQUEST_RECORD), Record2<Long?, JSONB?> {

    open var pullRequestId: Long
        set(value): Unit = set(0, value)
        get(): Long = get(0) as Long

    open var tags: JSONB?
        set(value): Unit = set(1, value)
        get(): JSONB? = get(1) as JSONB?

    // -------------------------------------------------------------------------
    // Record2 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row2<Long?, JSONB?> = super.fieldsRow() as Row2<Long?, JSONB?>
    override fun valuesRow(): Row2<Long?, JSONB?> = super.valuesRow() as Row2<Long?, JSONB?>
    override fun field1(): Field<Long?> = GithubPullRequestRecord.GITHUB_PULL_REQUEST_RECORD.PULL_REQUEST_ID
    override fun field2(): Field<JSONB?> = GithubPullRequestRecord.GITHUB_PULL_REQUEST_RECORD.TAGS
    override fun component1(): Long = pullRequestId
    override fun component2(): JSONB? = tags
    override fun value1(): Long = pullRequestId
    override fun value2(): JSONB? = tags

    override fun value1(value: Long?): GithubPullRequestRecordRecord {
        set(0, value)
        return this
    }

    override fun value2(value: JSONB?): GithubPullRequestRecordRecord {
        set(1, value)
        return this
    }

    override fun values(value1: Long?, value2: JSONB?): GithubPullRequestRecordRecord {
        this.value1(value1)
        this.value2(value2)
        return this
    }

    /**
     * Create a detached, initialised GithubPullRequestRecordRecord
     */
    constructor(pullRequestId: Long, tags: JSONB? = null): this() {
        this.pullRequestId = pullRequestId
        this.tags = tags
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised GithubPullRequestRecordRecord
     */
    constructor(value: com.arbr.db.datasets.tables.pojos.GithubPullRequestRecord?): this() {
        if (value != null) {
            this.pullRequestId = value.pullRequestId
            this.tags = value.tags
            resetChangedOnNotNull()
        }
    }
}