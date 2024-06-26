/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.records


import com.arbr.db.datasets.tables.GithubFileReference

import org.jooq.Field
import org.jooq.Record1
import org.jooq.Record5
import org.jooq.Row5
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class GithubFileReferenceRecord private constructor() : UpdatableRecordImpl<GithubFileReferenceRecord>(GithubFileReference.GITHUB_FILE_REFERENCE), Record5<Long?, String?, String?, String?, String?> {

    open var id: Long?
        set(value): Unit = set(0, value)
        get(): Long? = get(0) as Long?

    open var repoFullName: String
        set(value): Unit = set(1, value)
        get(): String = get(1) as String

    open var commitInfoSha: String
        set(value): Unit = set(2, value)
        get(): String = get(2) as String

    open var filename: String
        set(value): Unit = set(3, value)
        get(): String = get(3) as String

    open var uri: String
        set(value): Unit = set(4, value)
        get(): String = get(4) as String

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<Long?> = super.key() as Record1<Long?>

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row5<Long?, String?, String?, String?, String?> = super.fieldsRow() as Row5<Long?, String?, String?, String?, String?>
    override fun valuesRow(): Row5<Long?, String?, String?, String?, String?> = super.valuesRow() as Row5<Long?, String?, String?, String?, String?>
    override fun field1(): Field<Long?> = GithubFileReference.GITHUB_FILE_REFERENCE.ID
    override fun field2(): Field<String?> = GithubFileReference.GITHUB_FILE_REFERENCE.REPO_FULL_NAME
    override fun field3(): Field<String?> = GithubFileReference.GITHUB_FILE_REFERENCE.COMMIT_INFO_SHA
    override fun field4(): Field<String?> = GithubFileReference.GITHUB_FILE_REFERENCE.FILENAME
    override fun field5(): Field<String?> = GithubFileReference.GITHUB_FILE_REFERENCE.URI
    override fun component1(): Long? = id
    override fun component2(): String = repoFullName
    override fun component3(): String = commitInfoSha
    override fun component4(): String = filename
    override fun component5(): String = uri
    override fun value1(): Long? = id
    override fun value2(): String = repoFullName
    override fun value3(): String = commitInfoSha
    override fun value4(): String = filename
    override fun value5(): String = uri

    override fun value1(value: Long?): GithubFileReferenceRecord {
        set(0, value)
        return this
    }

    override fun value2(value: String?): GithubFileReferenceRecord {
        set(1, value)
        return this
    }

    override fun value3(value: String?): GithubFileReferenceRecord {
        set(2, value)
        return this
    }

    override fun value4(value: String?): GithubFileReferenceRecord {
        set(3, value)
        return this
    }

    override fun value5(value: String?): GithubFileReferenceRecord {
        set(4, value)
        return this
    }

    override fun values(value1: Long?, value2: String?, value3: String?, value4: String?, value5: String?): GithubFileReferenceRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        this.value5(value5)
        return this
    }

    /**
     * Create a detached, initialised GithubFileReferenceRecord
     */
    constructor(id: Long? = null, repoFullName: String, commitInfoSha: String, filename: String, uri: String): this() {
        this.id = id
        this.repoFullName = repoFullName
        this.commitInfoSha = commitInfoSha
        this.filename = filename
        this.uri = uri
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised GithubFileReferenceRecord
     */
    constructor(value: com.arbr.db.datasets.tables.pojos.GithubFileReference?): this() {
        if (value != null) {
            this.id = value.id
            this.repoFullName = value.repoFullName
            this.commitInfoSha = value.commitInfoSha
            this.filename = value.filename
            this.uri = value.uri
            resetChangedOnNotNull()
        }
    }
}
