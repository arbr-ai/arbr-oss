/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.records


import com.arbr.db.datasets.tables.GithubCommitStats

import org.jooq.Field
import org.jooq.Record4
import org.jooq.Row4
import org.jooq.impl.TableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class GithubCommitStatsRecord private constructor() : TableRecordImpl<GithubCommitStatsRecord>(GithubCommitStats.GITHUB_COMMIT_STATS), Record4<Long?, Long?, Long?, String?> {

    open var total: Long
        set(value): Unit = set(0, value)
        get(): Long = get(0) as Long

    open var additions: Long
        set(value): Unit = set(1, value)
        get(): Long = get(1) as Long

    open var deletions: Long
        set(value): Unit = set(2, value)
        get(): Long = get(2) as Long

    open var commitInfoSha: String
        set(value): Unit = set(3, value)
        get(): String = get(3) as String

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row4<Long?, Long?, Long?, String?> = super.fieldsRow() as Row4<Long?, Long?, Long?, String?>
    override fun valuesRow(): Row4<Long?, Long?, Long?, String?> = super.valuesRow() as Row4<Long?, Long?, Long?, String?>
    override fun field1(): Field<Long?> = GithubCommitStats.GITHUB_COMMIT_STATS.TOTAL
    override fun field2(): Field<Long?> = GithubCommitStats.GITHUB_COMMIT_STATS.ADDITIONS
    override fun field3(): Field<Long?> = GithubCommitStats.GITHUB_COMMIT_STATS.DELETIONS
    override fun field4(): Field<String?> = GithubCommitStats.GITHUB_COMMIT_STATS.COMMIT_INFO_SHA
    override fun component1(): Long = total
    override fun component2(): Long = additions
    override fun component3(): Long = deletions
    override fun component4(): String = commitInfoSha
    override fun value1(): Long = total
    override fun value2(): Long = additions
    override fun value3(): Long = deletions
    override fun value4(): String = commitInfoSha

    override fun value1(value: Long?): GithubCommitStatsRecord {
        set(0, value)
        return this
    }

    override fun value2(value: Long?): GithubCommitStatsRecord {
        set(1, value)
        return this
    }

    override fun value3(value: Long?): GithubCommitStatsRecord {
        set(2, value)
        return this
    }

    override fun value4(value: String?): GithubCommitStatsRecord {
        set(3, value)
        return this
    }

    override fun values(value1: Long?, value2: Long?, value3: Long?, value4: String?): GithubCommitStatsRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        return this
    }

    /**
     * Create a detached, initialised GithubCommitStatsRecord
     */
    constructor(total: Long, additions: Long, deletions: Long, commitInfoSha: String): this() {
        this.total = total
        this.additions = additions
        this.deletions = deletions
        this.commitInfoSha = commitInfoSha
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised GithubCommitStatsRecord
     */
    constructor(value: com.arbr.db.datasets.tables.pojos.GithubCommitStats?): this() {
        if (value != null) {
            this.total = value.total
            this.additions = value.additions
            this.deletions = value.deletions
            this.commitInfoSha = value.commitInfoSha
            resetChangedOnNotNull()
        }
    }
}
