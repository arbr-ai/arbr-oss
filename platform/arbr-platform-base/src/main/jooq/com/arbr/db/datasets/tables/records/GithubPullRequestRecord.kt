/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.records


import com.arbr.db.datasets.tables.GithubPullRequest

import org.jooq.Field
import org.jooq.Record1
import org.jooq.Record9
import org.jooq.Row9
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class GithubPullRequestRecord private constructor() : UpdatableRecordImpl<GithubPullRequestRecord>(GithubPullRequest.GITHUB_PULL_REQUEST), Record9<Long?, Long?, String?, String?, String?, String?, String?, String?, String?> {

    open var id: Long
        set(value): Unit = set(0, value)
        get(): Long = get(0) as Long

    open var repoId: Long
        set(value): Unit = set(1, value)
        get(): Long = get(1) as Long

    open var title: String
        set(value): Unit = set(2, value)
        get(): String = get(2) as String

    open var body: String?
        set(value): Unit = set(3, value)
        get(): String? = get(3) as String?

    open var diffUrl: String
        set(value): Unit = set(4, value)
        get(): String = get(4) as String

    open var htmlUrl: String
        set(value): Unit = set(5, value)
        get(): String = get(5) as String

    open var patchUrl: String
        set(value): Unit = set(6, value)
        get(): String = get(6) as String

    open var mergeCommitSha: String?
        set(value): Unit = set(7, value)
        get(): String? = get(7) as String?

    open var commitsUrl: String
        set(value): Unit = set(8, value)
        get(): String = get(8) as String

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<Long?> = super.key() as Record1<Long?>

    // -------------------------------------------------------------------------
    // Record9 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row9<Long?, Long?, String?, String?, String?, String?, String?, String?, String?> = super.fieldsRow() as Row9<Long?, Long?, String?, String?, String?, String?, String?, String?, String?>
    override fun valuesRow(): Row9<Long?, Long?, String?, String?, String?, String?, String?, String?, String?> = super.valuesRow() as Row9<Long?, Long?, String?, String?, String?, String?, String?, String?, String?>
    override fun field1(): Field<Long?> = GithubPullRequest.GITHUB_PULL_REQUEST.ID
    override fun field2(): Field<Long?> = GithubPullRequest.GITHUB_PULL_REQUEST.REPO_ID
    override fun field3(): Field<String?> = GithubPullRequest.GITHUB_PULL_REQUEST.TITLE
    override fun field4(): Field<String?> = GithubPullRequest.GITHUB_PULL_REQUEST.BODY
    override fun field5(): Field<String?> = GithubPullRequest.GITHUB_PULL_REQUEST.DIFF_URL
    override fun field6(): Field<String?> = GithubPullRequest.GITHUB_PULL_REQUEST.HTML_URL
    override fun field7(): Field<String?> = GithubPullRequest.GITHUB_PULL_REQUEST.PATCH_URL
    override fun field8(): Field<String?> = GithubPullRequest.GITHUB_PULL_REQUEST.MERGE_COMMIT_SHA
    override fun field9(): Field<String?> = GithubPullRequest.GITHUB_PULL_REQUEST.COMMITS_URL
    override fun component1(): Long = id
    override fun component2(): Long = repoId
    override fun component3(): String = title
    override fun component4(): String? = body
    override fun component5(): String = diffUrl
    override fun component6(): String = htmlUrl
    override fun component7(): String = patchUrl
    override fun component8(): String? = mergeCommitSha
    override fun component9(): String = commitsUrl
    override fun value1(): Long = id
    override fun value2(): Long = repoId
    override fun value3(): String = title
    override fun value4(): String? = body
    override fun value5(): String = diffUrl
    override fun value6(): String = htmlUrl
    override fun value7(): String = patchUrl
    override fun value8(): String? = mergeCommitSha
    override fun value9(): String = commitsUrl

    override fun value1(value: Long?): GithubPullRequestRecord {
        set(0, value)
        return this
    }

    override fun value2(value: Long?): GithubPullRequestRecord {
        set(1, value)
        return this
    }

    override fun value3(value: String?): GithubPullRequestRecord {
        set(2, value)
        return this
    }

    override fun value4(value: String?): GithubPullRequestRecord {
        set(3, value)
        return this
    }

    override fun value5(value: String?): GithubPullRequestRecord {
        set(4, value)
        return this
    }

    override fun value6(value: String?): GithubPullRequestRecord {
        set(5, value)
        return this
    }

    override fun value7(value: String?): GithubPullRequestRecord {
        set(6, value)
        return this
    }

    override fun value8(value: String?): GithubPullRequestRecord {
        set(7, value)
        return this
    }

    override fun value9(value: String?): GithubPullRequestRecord {
        set(8, value)
        return this
    }

    override fun values(value1: Long?, value2: Long?, value3: String?, value4: String?, value5: String?, value6: String?, value7: String?, value8: String?, value9: String?): GithubPullRequestRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        this.value5(value5)
        this.value6(value6)
        this.value7(value7)
        this.value8(value8)
        this.value9(value9)
        return this
    }

    /**
     * Create a detached, initialised GithubPullRequestRecord
     */
    constructor(id: Long, repoId: Long, title: String, body: String? = null, diffUrl: String, htmlUrl: String, patchUrl: String, mergeCommitSha: String? = null, commitsUrl: String): this() {
        this.id = id
        this.repoId = repoId
        this.title = title
        this.body = body
        this.diffUrl = diffUrl
        this.htmlUrl = htmlUrl
        this.patchUrl = patchUrl
        this.mergeCommitSha = mergeCommitSha
        this.commitsUrl = commitsUrl
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised GithubPullRequestRecord
     */
    constructor(value: com.arbr.db.datasets.tables.pojos.GithubPullRequest?): this() {
        if (value != null) {
            this.id = value.id
            this.repoId = value.repoId
            this.title = value.title
            this.body = value.body
            this.diffUrl = value.diffUrl
            this.htmlUrl = value.htmlUrl
            this.patchUrl = value.patchUrl
            this.mergeCommitSha = value.mergeCommitSha
            this.commitsUrl = value.commitsUrl
            resetChangedOnNotNull()
        }
    }
}