/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables


import com.arbr.db.datasets.Datasets
import com.arbr.db.datasets.keys.GITHUB_FILE_PARSE_RULE_CONTEXT_PKEY
import com.arbr.db.datasets.keys.GITHUB_FILE_PARSE_RULE_CONTEXT__GITHUB_FILE_PARSE_RULE_CONTEXT_START_TOKEN_ID_FKEY
import com.arbr.db.datasets.keys.GITHUB_FILE_PARSE_RULE_CONTEXT__GITHUB_FILE_PARSE_RULE_CONTEXT_STOP_TOKEN_ID_FKEY
import com.arbr.db.datasets.tables.records.GithubFileParseRuleContextRecord

import java.util.function.Function

import kotlin.collections.List

import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Identity
import org.jooq.Name
import org.jooq.Record
import org.jooq.Records
import org.jooq.Row5
import org.jooq.Schema
import org.jooq.SelectField
import org.jooq.Table
import org.jooq.TableField
import org.jooq.TableOptions
import org.jooq.UniqueKey
import org.jooq.impl.DSL
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType
import org.jooq.impl.TableImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class GithubFileParseRuleContext(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, GithubFileParseRuleContextRecord>?,
    aliased: Table<GithubFileParseRuleContextRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<GithubFileParseRuleContextRecord>(
    alias,
    Datasets.DATASETS,
    child,
    path,
    aliased,
    parameters,
    DSL.comment(""),
    TableOptions.table()
) {
    companion object {

        /**
         * The reference instance of
         * <code>datasets.github_file_parse_rule_context</code>
         */
        val GITHUB_FILE_PARSE_RULE_CONTEXT: GithubFileParseRuleContext = GithubFileParseRuleContext()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<GithubFileParseRuleContextRecord> = GithubFileParseRuleContextRecord::class.java

    /**
     * The column <code>datasets.github_file_parse_rule_context.id</code>.
     */
    val ID: TableField<GithubFileParseRuleContextRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "")

    /**
     * The column
     * <code>datasets.github_file_parse_rule_context.rule_index</code>.
     */
    val RULE_INDEX: TableField<GithubFileParseRuleContextRecord, Int?> = createField(DSL.name("rule_index"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column
     * <code>datasets.github_file_parse_rule_context.child_count</code>.
     */
    val CHILD_COUNT: TableField<GithubFileParseRuleContextRecord, Int?> = createField(DSL.name("child_count"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column
     * <code>datasets.github_file_parse_rule_context.start_token_id</code>.
     */
    val START_TOKEN_ID: TableField<GithubFileParseRuleContextRecord, Long?> = createField(DSL.name("start_token_id"), SQLDataType.BIGINT, this, "")

    /**
     * The column
     * <code>datasets.github_file_parse_rule_context.stop_token_id</code>.
     */
    val STOP_TOKEN_ID: TableField<GithubFileParseRuleContextRecord, Long?> = createField(DSL.name("stop_token_id"), SQLDataType.BIGINT, this, "")

    private constructor(alias: Name, aliased: Table<GithubFileParseRuleContextRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<GithubFileParseRuleContextRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>datasets.github_file_parse_rule_context</code>
     * table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>datasets.github_file_parse_rule_context</code>
     * table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>datasets.github_file_parse_rule_context</code> table
     * reference
     */
    constructor(): this(DSL.name("github_file_parse_rule_context"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, GithubFileParseRuleContextRecord>): this(Internal.createPathAlias(child, key), child, key, GITHUB_FILE_PARSE_RULE_CONTEXT, null)
    override fun getSchema(): Schema? = if (aliased()) null else Datasets.DATASETS
    override fun getIdentity(): Identity<GithubFileParseRuleContextRecord, Long?> = super.getIdentity() as Identity<GithubFileParseRuleContextRecord, Long?>
    override fun getPrimaryKey(): UniqueKey<GithubFileParseRuleContextRecord> = GITHUB_FILE_PARSE_RULE_CONTEXT_PKEY
    override fun getReferences(): List<ForeignKey<GithubFileParseRuleContextRecord, *>> = listOf(GITHUB_FILE_PARSE_RULE_CONTEXT__GITHUB_FILE_PARSE_RULE_CONTEXT_START_TOKEN_ID_FKEY, GITHUB_FILE_PARSE_RULE_CONTEXT__GITHUB_FILE_PARSE_RULE_CONTEXT_STOP_TOKEN_ID_FKEY)

    private lateinit var _githubFileParseRuleContextStartTokenIdFkey: GithubFileParseToken
    private lateinit var _githubFileParseRuleContextStopTokenIdFkey: GithubFileParseToken

    /**
     * Get the implicit join path to the
     * <code>datasets.github_file_parse_token</code> table, via the
     * <code>github_file_parse_rule_context_start_token_id_fkey</code> key.
     */
    fun githubFileParseRuleContextStartTokenIdFkey(): GithubFileParseToken {
        if (!this::_githubFileParseRuleContextStartTokenIdFkey.isInitialized)
            _githubFileParseRuleContextStartTokenIdFkey = GithubFileParseToken(this, GITHUB_FILE_PARSE_RULE_CONTEXT__GITHUB_FILE_PARSE_RULE_CONTEXT_START_TOKEN_ID_FKEY)

        return _githubFileParseRuleContextStartTokenIdFkey;
    }

    val githubFileParseRuleContextStartTokenIdFkey: GithubFileParseToken
        get(): GithubFileParseToken = githubFileParseRuleContextStartTokenIdFkey()

    /**
     * Get the implicit join path to the
     * <code>datasets.github_file_parse_token</code> table, via the
     * <code>github_file_parse_rule_context_stop_token_id_fkey</code> key.
     */
    fun githubFileParseRuleContextStopTokenIdFkey(): GithubFileParseToken {
        if (!this::_githubFileParseRuleContextStopTokenIdFkey.isInitialized)
            _githubFileParseRuleContextStopTokenIdFkey = GithubFileParseToken(this, GITHUB_FILE_PARSE_RULE_CONTEXT__GITHUB_FILE_PARSE_RULE_CONTEXT_STOP_TOKEN_ID_FKEY)

        return _githubFileParseRuleContextStopTokenIdFkey;
    }

    val githubFileParseRuleContextStopTokenIdFkey: GithubFileParseToken
        get(): GithubFileParseToken = githubFileParseRuleContextStopTokenIdFkey()
    override fun `as`(alias: String): GithubFileParseRuleContext = GithubFileParseRuleContext(DSL.name(alias), this)
    override fun `as`(alias: Name): GithubFileParseRuleContext = GithubFileParseRuleContext(alias, this)
    override fun `as`(alias: Table<*>): GithubFileParseRuleContext = GithubFileParseRuleContext(alias.getQualifiedName(), this)

    /**
     * Rename this table
     */
    override fun rename(name: String): GithubFileParseRuleContext = GithubFileParseRuleContext(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): GithubFileParseRuleContext = GithubFileParseRuleContext(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): GithubFileParseRuleContext = GithubFileParseRuleContext(name.getQualifiedName(), null)

    // -------------------------------------------------------------------------
    // Row5 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row5<Long?, Int?, Int?, Long?, Long?> = super.fieldsRow() as Row5<Long?, Int?, Int?, Long?, Long?>

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    fun <U> mapping(from: (Long?, Int?, Int?, Long?, Long?) -> U): SelectField<U> = convertFrom(Records.mapping(from))

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    fun <U> mapping(toType: Class<U>, from: (Long?, Int?, Int?, Long?, Long?) -> U): SelectField<U> = convertFrom(toType, Records.mapping(from))
}
