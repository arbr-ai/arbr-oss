/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.records


import com.arbr.db.datasets.tables.GithubFileParseRuleContext

import org.jooq.Field
import org.jooq.Record1
import org.jooq.Record5
import org.jooq.Row5
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class GithubFileParseRuleContextRecord private constructor() : UpdatableRecordImpl<GithubFileParseRuleContextRecord>(GithubFileParseRuleContext.GITHUB_FILE_PARSE_RULE_CONTEXT), Record5<Long?, Int?, Int?, Long?, Long?> {

    open var id: Long?
        set(value): Unit = set(0, value)
        get(): Long? = get(0) as Long?

    open var ruleIndex: Int
        set(value): Unit = set(1, value)
        get(): Int = get(1) as Int

    open var childCount: Int
        set(value): Unit = set(2, value)
        get(): Int = get(2) as Int

    open var startTokenId: Long?
        set(value): Unit = set(3, value)
        get(): Long? = get(3) as Long?

    open var stopTokenId: Long?
        set(value): Unit = set(4, value)
        get(): Long? = get(4) as Long?

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<Long?> = super.key() as Record1<Long?>

    // -------------------------------------------------------------------------
    // Record5 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row5<Long?, Int?, Int?, Long?, Long?> = super.fieldsRow() as Row5<Long?, Int?, Int?, Long?, Long?>
    override fun valuesRow(): Row5<Long?, Int?, Int?, Long?, Long?> = super.valuesRow() as Row5<Long?, Int?, Int?, Long?, Long?>
    override fun field1(): Field<Long?> = GithubFileParseRuleContext.GITHUB_FILE_PARSE_RULE_CONTEXT.ID
    override fun field2(): Field<Int?> = GithubFileParseRuleContext.GITHUB_FILE_PARSE_RULE_CONTEXT.RULE_INDEX
    override fun field3(): Field<Int?> = GithubFileParseRuleContext.GITHUB_FILE_PARSE_RULE_CONTEXT.CHILD_COUNT
    override fun field4(): Field<Long?> = GithubFileParseRuleContext.GITHUB_FILE_PARSE_RULE_CONTEXT.START_TOKEN_ID
    override fun field5(): Field<Long?> = GithubFileParseRuleContext.GITHUB_FILE_PARSE_RULE_CONTEXT.STOP_TOKEN_ID
    override fun component1(): Long? = id
    override fun component2(): Int = ruleIndex
    override fun component3(): Int = childCount
    override fun component4(): Long? = startTokenId
    override fun component5(): Long? = stopTokenId
    override fun value1(): Long? = id
    override fun value2(): Int = ruleIndex
    override fun value3(): Int = childCount
    override fun value4(): Long? = startTokenId
    override fun value5(): Long? = stopTokenId

    override fun value1(value: Long?): GithubFileParseRuleContextRecord {
        set(0, value)
        return this
    }

    override fun value2(value: Int?): GithubFileParseRuleContextRecord {
        set(1, value)
        return this
    }

    override fun value3(value: Int?): GithubFileParseRuleContextRecord {
        set(2, value)
        return this
    }

    override fun value4(value: Long?): GithubFileParseRuleContextRecord {
        set(3, value)
        return this
    }

    override fun value5(value: Long?): GithubFileParseRuleContextRecord {
        set(4, value)
        return this
    }

    override fun values(value1: Long?, value2: Int?, value3: Int?, value4: Long?, value5: Long?): GithubFileParseRuleContextRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        this.value5(value5)
        return this
    }

    /**
     * Create a detached, initialised GithubFileParseRuleContextRecord
     */
    constructor(id: Long? = null, ruleIndex: Int, childCount: Int, startTokenId: Long? = null, stopTokenId: Long? = null): this() {
        this.id = id
        this.ruleIndex = ruleIndex
        this.childCount = childCount
        this.startTokenId = startTokenId
        this.stopTokenId = stopTokenId
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised GithubFileParseRuleContextRecord
     */
    constructor(value: com.arbr.db.datasets.tables.pojos.GithubFileParseRuleContext?): this() {
        if (value != null) {
            this.id = value.id
            this.ruleIndex = value.ruleIndex
            this.childCount = value.childCount
            this.startTokenId = value.startTokenId
            this.stopTokenId = value.stopTokenId
            resetChangedOnNotNull()
        }
    }
}
