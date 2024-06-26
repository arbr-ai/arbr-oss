/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables


import java.util.function.Function

import kotlin.collections.List

import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Identity
import org.jooq.Index
import org.jooq.JSONB
import org.jooq.Name
import org.jooq.Record
import org.jooq.Records
import org.jooq.Row13
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
open class UserProjectWorkflow(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord>?,
    aliased: Table<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord>(
    alias,
    com.arbr.db.`public`.Public.PUBLIC,
    child,
    path,
    aliased,
    parameters,
    DSL.comment(""),
    TableOptions.table()
) {
    companion object {

        /**
         * The reference instance of <code>public.user_project_workflow</code>
         */
        val USER_PROJECT_WORKFLOW: UserProjectWorkflow = UserProjectWorkflow()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord> = com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord::class.java

    /**
     * The column <code>public.user_project_workflow.id</code>.
     */
    val ID: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "")

    /**
     * The column <code>public.user_project_workflow.project_id</code>.
     */
    val PROJECT_ID: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, Long?> = createField(DSL.name("project_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>public.user_project_workflow.creation_timestamp</code>.
     */
    val CREATION_TIMESTAMP: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, Long?> = createField(DSL.name("creation_timestamp"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>public.user_project_workflow.workflow_type</code>.
     */
    val WORKFLOW_TYPE: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, String?> = createField(DSL.name("workflow_type"), SQLDataType.VARCHAR(511).nullable(false), this, "")

    /**
     * The column <code>public.user_project_workflow.last_status</code>.
     */
    val LAST_STATUS: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, Int?> = createField(DSL.name("last_status"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column
     * <code>public.user_project_workflow.last_status_recorded_timestamp</code>.
     */
    val LAST_STATUS_RECORDED_TIMESTAMP: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, Long?> = createField(DSL.name("last_status_recorded_timestamp"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>public.user_project_workflow.plan_info</code>.
     */
    val PLAN_INFO: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, JSONB?> = createField(DSL.name("plan_info"), SQLDataType.JSONB.nullable(false), this, "")

    /**
     * The column <code>public.user_project_workflow.commit_info</code>.
     */
    val COMMIT_INFO: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, JSONB?> = createField(DSL.name("commit_info"), SQLDataType.JSONB.nullable(false), this, "")

    /**
     * The column
     * <code>public.user_project_workflow.requested_user_inputs</code>.
     */
    val REQUESTED_USER_INPUTS: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, JSONB?> = createField(DSL.name("requested_user_inputs"), SQLDataType.JSONB.nullable(false), this, "")

    /**
     * The column <code>public.user_project_workflow.valued_user_inputs</code>.
     */
    val VALUED_USER_INPUTS: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, JSONB?> = createField(DSL.name("valued_user_inputs"), SQLDataType.JSONB.nullable(false), this, "")

    /**
     * The column <code>public.user_project_workflow.build_artifacts</code>.
     */
    val BUILD_ARTIFACTS: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, JSONB?> = createField(DSL.name("build_artifacts"), SQLDataType.JSONB.nullable(false), this, "")

    /**
     * The column <code>public.user_project_workflow.param_map</code>.
     */
    val PARAM_MAP: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, JSONB?> = createField(DSL.name("param_map"), SQLDataType.JSONB, this, "")

    /**
     * The column <code>public.user_project_workflow.idempotency_key</code>.
     */
    val IDEMPOTENCY_KEY: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, String?> = createField(DSL.name("idempotency_key"), SQLDataType.VARCHAR(511), this, "")

    private constructor(alias: Name, aliased: Table<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>public.user_project_workflow</code> table
     * reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>public.user_project_workflow</code> table
     * reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>public.user_project_workflow</code> table reference
     */
    constructor(): this(DSL.name("user_project_workflow"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord>): this(Internal.createPathAlias(child, key), child, key, USER_PROJECT_WORKFLOW, null)
    override fun getSchema(): Schema? = if (aliased()) null else com.arbr.db.`public`.Public.PUBLIC
    override fun getIndexes(): List<Index> = listOf(com.arbr.db.`public`.indexes.USER_PROJECT_WORKFLOW_ID_IDX, com.arbr.db.`public`.indexes.USER_PROJECT_WORKFLOW_PROJECT_ID_IDX)
    override fun getIdentity(): Identity<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, Long?> = super.getIdentity() as Identity<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, Long?>
    override fun getUniqueKeys(): List<UniqueKey<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord>> = listOf(com.arbr.db.`public`.keys.USER_PROJECT_WORKFLOW_ID_KEY)
    override fun getReferences(): List<ForeignKey<com.arbr.db.`public`.tables.records.UserProjectWorkflowRecord, *>> = listOf(com.arbr.db.`public`.keys.USER_PROJECT_WORKFLOW__USER_PROJECT_WORKFLOW_PROJECT_ID_FKEY)

    private lateinit var _userProject: com.arbr.db.`public`.tables.UserProject

    /**
     * Get the implicit join path to the <code>public.user_project</code> table.
     */
    fun userProject(): com.arbr.db.`public`.tables.UserProject {
        if (!this::_userProject.isInitialized)
            _userProject = com.arbr.db.`public`.tables.UserProject(this, com.arbr.db.`public`.keys.USER_PROJECT_WORKFLOW__USER_PROJECT_WORKFLOW_PROJECT_ID_FKEY)

        return _userProject;
    }

    val userProject: com.arbr.db.`public`.tables.UserProject
        get(): com.arbr.db.`public`.tables.UserProject = userProject()
    override fun `as`(alias: String): UserProjectWorkflow = UserProjectWorkflow(DSL.name(alias), this)
    override fun `as`(alias: Name): UserProjectWorkflow = UserProjectWorkflow(alias, this)
    override fun `as`(alias: Table<*>): UserProjectWorkflow = UserProjectWorkflow(alias.getQualifiedName(), this)

    /**
     * Rename this table
     */
    override fun rename(name: String): UserProjectWorkflow = UserProjectWorkflow(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): UserProjectWorkflow = UserProjectWorkflow(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): UserProjectWorkflow = UserProjectWorkflow(name.getQualifiedName(), null)

    // -------------------------------------------------------------------------
    // Row13 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row13<Long?, Long?, Long?, String?, Int?, Long?, JSONB?, JSONB?, JSONB?, JSONB?, JSONB?, JSONB?, String?> = super.fieldsRow() as Row13<Long?, Long?, Long?, String?, Int?, Long?, JSONB?, JSONB?, JSONB?, JSONB?, JSONB?, JSONB?, String?>

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    fun <U> mapping(from: (Long?, Long?, Long?, String?, Int?, Long?, JSONB?, JSONB?, JSONB?, JSONB?, JSONB?, JSONB?, String?) -> U): SelectField<U> = convertFrom(Records.mapping(from))

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    fun <U> mapping(toType: Class<U>, from: (Long?, Long?, Long?, String?, Int?, Long?, JSONB?, JSONB?, JSONB?, JSONB?, JSONB?, JSONB?, String?) -> U): SelectField<U> = convertFrom(toType, Records.mapping(from))
}
