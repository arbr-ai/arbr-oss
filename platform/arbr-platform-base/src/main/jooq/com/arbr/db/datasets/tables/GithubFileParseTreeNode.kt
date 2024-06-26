/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.datasets.tables


import com.arbr.db.datasets.Datasets
import com.arbr.db.datasets.keys.GITHUB_FILE_PARSE_TREE_NODE_PKEY
import com.arbr.db.datasets.keys.GITHUB_FILE_PARSE_TREE_NODE__GITHUB_FILE_PARSE_TREE_NODE_FILE_ID_FKEY
import com.arbr.db.datasets.keys.GITHUB_FILE_PARSE_TREE_NODE__GITHUB_FILE_PARSE_TREE_NODE_RULE_CONTEXT_ID_FKEY
import com.arbr.db.datasets.keys.GITHUB_FILE_PARSE_TREE_NODE__GITHUB_FILE_PARSE_TREE_NODE_TERMINAL_NODE_ID_FKEY
import com.arbr.db.datasets.tables.records.GithubFileParseTreeNodeRecord

import java.util.function.Function

import kotlin.collections.List

import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Identity
import org.jooq.Name
import org.jooq.Record
import org.jooq.Records
import org.jooq.Row8
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
open class GithubFileParseTreeNode(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, GithubFileParseTreeNodeRecord>?,
    aliased: Table<GithubFileParseTreeNodeRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<GithubFileParseTreeNodeRecord>(
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
         * <code>datasets.github_file_parse_tree_node</code>
         */
        val GITHUB_FILE_PARSE_TREE_NODE: GithubFileParseTreeNode = GithubFileParseTreeNode()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<GithubFileParseTreeNodeRecord> = GithubFileParseTreeNodeRecord::class.java

    /**
     * The column <code>datasets.github_file_parse_tree_node.id</code>.
     */
    val ID: TableField<GithubFileParseTreeNodeRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "")

    /**
     * The column <code>datasets.github_file_parse_tree_node.file_id</code>.
     */
    val FILE_ID: TableField<GithubFileParseTreeNodeRecord, Long?> = createField(DSL.name("file_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>datasets.github_file_parse_tree_node.filename</code>.
     */
    val FILENAME: TableField<GithubFileParseTreeNodeRecord, String?> = createField(DSL.name("filename"), SQLDataType.CLOB.nullable(false), this, "")

    /**
     * The column <code>datasets.github_file_parse_tree_node.push_index</code>.
     */
    val PUSH_INDEX: TableField<GithubFileParseTreeNodeRecord, Int?> = createField(DSL.name("push_index"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column <code>datasets.github_file_parse_tree_node.pop_index</code>.
     */
    val POP_INDEX: TableField<GithubFileParseTreeNodeRecord, Int?> = createField(DSL.name("pop_index"), SQLDataType.INTEGER, this, "")

    /**
     * The column
     * <code>datasets.github_file_parse_tree_node.parent_push_index</code>.
     */
    val PARENT_PUSH_INDEX: TableField<GithubFileParseTreeNodeRecord, Int?> = createField(DSL.name("parent_push_index"), SQLDataType.INTEGER, this, "")

    /**
     * The column
     * <code>datasets.github_file_parse_tree_node.rule_context_id</code>.
     */
    val RULE_CONTEXT_ID: TableField<GithubFileParseTreeNodeRecord, Long?> = createField(DSL.name("rule_context_id"), SQLDataType.BIGINT, this, "")

    /**
     * The column
     * <code>datasets.github_file_parse_tree_node.terminal_node_id</code>.
     */
    val TERMINAL_NODE_ID: TableField<GithubFileParseTreeNodeRecord, Long?> = createField(DSL.name("terminal_node_id"), SQLDataType.BIGINT, this, "")

    private constructor(alias: Name, aliased: Table<GithubFileParseTreeNodeRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<GithubFileParseTreeNodeRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>datasets.github_file_parse_tree_node</code> table
     * reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>datasets.github_file_parse_tree_node</code> table
     * reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>datasets.github_file_parse_tree_node</code> table
     * reference
     */
    constructor(): this(DSL.name("github_file_parse_tree_node"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, GithubFileParseTreeNodeRecord>): this(Internal.createPathAlias(child, key), child, key, GITHUB_FILE_PARSE_TREE_NODE, null)
    override fun getSchema(): Schema? = if (aliased()) null else Datasets.DATASETS
    override fun getIdentity(): Identity<GithubFileParseTreeNodeRecord, Long?> = super.getIdentity() as Identity<GithubFileParseTreeNodeRecord, Long?>
    override fun getPrimaryKey(): UniqueKey<GithubFileParseTreeNodeRecord> = GITHUB_FILE_PARSE_TREE_NODE_PKEY
    override fun getReferences(): List<ForeignKey<GithubFileParseTreeNodeRecord, *>> = listOf(GITHUB_FILE_PARSE_TREE_NODE__GITHUB_FILE_PARSE_TREE_NODE_FILE_ID_FKEY, GITHUB_FILE_PARSE_TREE_NODE__GITHUB_FILE_PARSE_TREE_NODE_RULE_CONTEXT_ID_FKEY, GITHUB_FILE_PARSE_TREE_NODE__GITHUB_FILE_PARSE_TREE_NODE_TERMINAL_NODE_ID_FKEY)

    private lateinit var _githubFileReference: GithubFileReference
    private lateinit var _githubFileParseRuleContext: GithubFileParseRuleContext
    private lateinit var _githubFileParseTerminalNode: GithubFileParseTerminalNode

    /**
     * Get the implicit join path to the
     * <code>datasets.github_file_reference</code> table.
     */
    fun githubFileReference(): GithubFileReference {
        if (!this::_githubFileReference.isInitialized)
            _githubFileReference = GithubFileReference(this, GITHUB_FILE_PARSE_TREE_NODE__GITHUB_FILE_PARSE_TREE_NODE_FILE_ID_FKEY)

        return _githubFileReference;
    }

    val githubFileReference: GithubFileReference
        get(): GithubFileReference = githubFileReference()

    /**
     * Get the implicit join path to the
     * <code>datasets.github_file_parse_rule_context</code> table.
     */
    fun githubFileParseRuleContext(): GithubFileParseRuleContext {
        if (!this::_githubFileParseRuleContext.isInitialized)
            _githubFileParseRuleContext = GithubFileParseRuleContext(this, GITHUB_FILE_PARSE_TREE_NODE__GITHUB_FILE_PARSE_TREE_NODE_RULE_CONTEXT_ID_FKEY)

        return _githubFileParseRuleContext;
    }

    val githubFileParseRuleContext: GithubFileParseRuleContext
        get(): GithubFileParseRuleContext = githubFileParseRuleContext()

    /**
     * Get the implicit join path to the
     * <code>datasets.github_file_parse_terminal_node</code> table.
     */
    fun githubFileParseTerminalNode(): GithubFileParseTerminalNode {
        if (!this::_githubFileParseTerminalNode.isInitialized)
            _githubFileParseTerminalNode = GithubFileParseTerminalNode(this, GITHUB_FILE_PARSE_TREE_NODE__GITHUB_FILE_PARSE_TREE_NODE_TERMINAL_NODE_ID_FKEY)

        return _githubFileParseTerminalNode;
    }

    val githubFileParseTerminalNode: GithubFileParseTerminalNode
        get(): GithubFileParseTerminalNode = githubFileParseTerminalNode()
    override fun `as`(alias: String): GithubFileParseTreeNode = GithubFileParseTreeNode(DSL.name(alias), this)
    override fun `as`(alias: Name): GithubFileParseTreeNode = GithubFileParseTreeNode(alias, this)
    override fun `as`(alias: Table<*>): GithubFileParseTreeNode = GithubFileParseTreeNode(alias.getQualifiedName(), this)

    /**
     * Rename this table
     */
    override fun rename(name: String): GithubFileParseTreeNode = GithubFileParseTreeNode(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): GithubFileParseTreeNode = GithubFileParseTreeNode(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): GithubFileParseTreeNode = GithubFileParseTreeNode(name.getQualifiedName(), null)

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row8<Long?, Long?, String?, Int?, Int?, Int?, Long?, Long?> = super.fieldsRow() as Row8<Long?, Long?, String?, Int?, Int?, Int?, Long?, Long?>

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    fun <U> mapping(from: (Long?, Long?, String?, Int?, Int?, Int?, Long?, Long?) -> U): SelectField<U> = convertFrom(Records.mapping(from))

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    fun <U> mapping(toType: Class<U>, from: (Long?, Long?, String?, Int?, Int?, Int?, Long?, Long?) -> U): SelectField<U> = convertFrom(toType, Records.mapping(from))
}
