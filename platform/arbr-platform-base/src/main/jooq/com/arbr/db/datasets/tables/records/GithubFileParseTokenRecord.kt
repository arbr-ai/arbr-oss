/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables.records


import com.arbr.db.datasets.tables.GithubFileParseToken

import org.jooq.Field
import org.jooq.Record1
import org.jooq.Record10
import org.jooq.Row10
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class GithubFileParseTokenRecord private constructor() : UpdatableRecordImpl<GithubFileParseTokenRecord>(GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN), Record10<Long?, Int?, String?, Int?, Int?, Int?, Int?, Int?, Int?, Int?> {

    open var id: Long?
        set(value): Unit = set(0, value)
        get(): Long? = get(0) as Long?

    open var type: Int
        set(value): Unit = set(1, value)
        get(): Int = get(1) as Int

    open var text: String
        set(value): Unit = set(2, value)
        get(): String = get(2) as String

    open var line: Int
        set(value): Unit = set(3, value)
        get(): Int = get(3) as Int

    open var charPositionInLine: Int
        set(value): Unit = set(4, value)
        get(): Int = get(4) as Int

    open var channel: Int
        set(value): Unit = set(5, value)
        get(): Int = get(5) as Int

    open var tokenIndex: Int
        set(value): Unit = set(6, value)
        get(): Int = get(6) as Int

    open var startIndex: Int
        set(value): Unit = set(7, value)
        get(): Int = get(7) as Int

    open var stopIndex: Int
        set(value): Unit = set(8, value)
        get(): Int = get(8) as Int

    open var locus: Int?
        set(value): Unit = set(9, value)
        get(): Int? = get(9) as Int?

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<Long?> = super.key() as Record1<Long?>

    // -------------------------------------------------------------------------
    // Record10 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row10<Long?, Int?, String?, Int?, Int?, Int?, Int?, Int?, Int?, Int?> = super.fieldsRow() as Row10<Long?, Int?, String?, Int?, Int?, Int?, Int?, Int?, Int?, Int?>
    override fun valuesRow(): Row10<Long?, Int?, String?, Int?, Int?, Int?, Int?, Int?, Int?, Int?> = super.valuesRow() as Row10<Long?, Int?, String?, Int?, Int?, Int?, Int?, Int?, Int?, Int?>
    override fun field1(): Field<Long?> = GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN.ID
    override fun field2(): Field<Int?> = GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN.TYPE
    override fun field3(): Field<String?> = GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN.TEXT
    override fun field4(): Field<Int?> = GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN.LINE
    override fun field5(): Field<Int?> = GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN.CHAR_POSITION_IN_LINE
    override fun field6(): Field<Int?> = GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN.CHANNEL
    override fun field7(): Field<Int?> = GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN.TOKEN_INDEX
    override fun field8(): Field<Int?> = GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN.START_INDEX
    override fun field9(): Field<Int?> = GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN.STOP_INDEX
    override fun field10(): Field<Int?> = GithubFileParseToken.GITHUB_FILE_PARSE_TOKEN.LOCUS
    override fun component1(): Long? = id
    override fun component2(): Int = type
    override fun component3(): String = text
    override fun component4(): Int = line
    override fun component5(): Int = charPositionInLine
    override fun component6(): Int = channel
    override fun component7(): Int = tokenIndex
    override fun component8(): Int = startIndex
    override fun component9(): Int = stopIndex
    override fun component10(): Int? = locus
    override fun value1(): Long? = id
    override fun value2(): Int = type
    override fun value3(): String = text
    override fun value4(): Int = line
    override fun value5(): Int = charPositionInLine
    override fun value6(): Int = channel
    override fun value7(): Int = tokenIndex
    override fun value8(): Int = startIndex
    override fun value9(): Int = stopIndex
    override fun value10(): Int? = locus

    override fun value1(value: Long?): GithubFileParseTokenRecord {
        set(0, value)
        return this
    }

    override fun value2(value: Int?): GithubFileParseTokenRecord {
        set(1, value)
        return this
    }

    override fun value3(value: String?): GithubFileParseTokenRecord {
        set(2, value)
        return this
    }

    override fun value4(value: Int?): GithubFileParseTokenRecord {
        set(3, value)
        return this
    }

    override fun value5(value: Int?): GithubFileParseTokenRecord {
        set(4, value)
        return this
    }

    override fun value6(value: Int?): GithubFileParseTokenRecord {
        set(5, value)
        return this
    }

    override fun value7(value: Int?): GithubFileParseTokenRecord {
        set(6, value)
        return this
    }

    override fun value8(value: Int?): GithubFileParseTokenRecord {
        set(7, value)
        return this
    }

    override fun value9(value: Int?): GithubFileParseTokenRecord {
        set(8, value)
        return this
    }

    override fun value10(value: Int?): GithubFileParseTokenRecord {
        set(9, value)
        return this
    }

    override fun values(value1: Long?, value2: Int?, value3: String?, value4: Int?, value5: Int?, value6: Int?, value7: Int?, value8: Int?, value9: Int?, value10: Int?): GithubFileParseTokenRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        this.value5(value5)
        this.value6(value6)
        this.value7(value7)
        this.value8(value8)
        this.value9(value9)
        this.value10(value10)
        return this
    }

    /**
     * Create a detached, initialised GithubFileParseTokenRecord
     */
    constructor(id: Long? = null, type: Int, text: String, line: Int, charPositionInLine: Int, channel: Int, tokenIndex: Int, startIndex: Int, stopIndex: Int, locus: Int? = null): this() {
        this.id = id
        this.type = type
        this.text = text
        this.line = line
        this.charPositionInLine = charPositionInLine
        this.channel = channel
        this.tokenIndex = tokenIndex
        this.startIndex = startIndex
        this.stopIndex = stopIndex
        this.locus = locus
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised GithubFileParseTokenRecord
     */
    constructor(value: com.arbr.db.datasets.tables.pojos.GithubFileParseToken?): this() {
        if (value != null) {
            this.id = value.id
            this.type = value.type
            this.text = value.text
            this.line = value.line
            this.charPositionInLine = value.charPositionInLine
            this.channel = value.channel
            this.tokenIndex = value.tokenIndex
            this.startIndex = value.startIndex
            this.stopIndex = value.stopIndex
            this.locus = value.locus
            resetChangedOnNotNull()
        }
    }
}
