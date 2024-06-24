/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.records


import com.arbr.db.datasets.tables.GithubFileParseTree

import org.jooq.Field
import org.jooq.JSONB
import org.jooq.Record3
import org.jooq.Row3
import org.jooq.impl.TableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class GithubFileParseTreeRecord private constructor() : TableRecordImpl<GithubFileParseTreeRecord>(GithubFileParseTree.GITHUB_FILE_PARSE_TREE), Record3<String?, String?, JSONB?> {

    open var commitInfoSha: String?
        set(value): Unit = set(0, value)
        get(): String? = get(0) as String?

    open var filename: String
        set(value): Unit = set(1, value)
        get(): String = get(1) as String

    open var parseTree: JSONB
        set(value): Unit = set(2, value)
        get(): JSONB = get(2) as JSONB

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row3<String?, String?, JSONB?> = super.fieldsRow() as Row3<String?, String?, JSONB?>
    override fun valuesRow(): Row3<String?, String?, JSONB?> = super.valuesRow() as Row3<String?, String?, JSONB?>
    override fun field1(): Field<String?> = GithubFileParseTree.GITHUB_FILE_PARSE_TREE.COMMIT_INFO_SHA
    override fun field2(): Field<String?> = GithubFileParseTree.GITHUB_FILE_PARSE_TREE.FILENAME
    override fun field3(): Field<JSONB?> = GithubFileParseTree.GITHUB_FILE_PARSE_TREE.PARSE_TREE
    override fun component1(): String? = commitInfoSha
    override fun component2(): String = filename
    override fun component3(): JSONB = parseTree
    override fun value1(): String? = commitInfoSha
    override fun value2(): String = filename
    override fun value3(): JSONB = parseTree

    override fun value1(value: String?): GithubFileParseTreeRecord {
        set(0, value)
        return this
    }

    override fun value2(value: String?): GithubFileParseTreeRecord {
        set(1, value)
        return this
    }

    override fun value3(value: JSONB?): GithubFileParseTreeRecord {
        set(2, value)
        return this
    }

    override fun values(value1: String?, value2: String?, value3: JSONB?): GithubFileParseTreeRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        return this
    }

    /**
     * Create a detached, initialised GithubFileParseTreeRecord
     */
    constructor(commitInfoSha: String? = null, filename: String, parseTree: JSONB): this() {
        this.commitInfoSha = commitInfoSha
        this.filename = filename
        this.parseTree = parseTree
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised GithubFileParseTreeRecord
     */
    constructor(value: com.arbr.db.datasets.tables.pojos.GithubFileParseTree?): this() {
        if (value != null) {
            this.commitInfoSha = value.commitInfoSha
            this.filename = value.filename
            this.parseTree = value.parseTree
            resetChangedOnNotNull()
        }
    }
}