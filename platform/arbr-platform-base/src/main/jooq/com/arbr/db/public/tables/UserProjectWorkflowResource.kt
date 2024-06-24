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
import org.jooq.Row10
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
open class UserProjectWorkflowResource(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord>?,
    aliased: Table<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord>(
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
         * The reference instance of
         * <code>public.user_project_workflow_resource</code>
         */
        val USER_PROJECT_WORKFLOW_RESOURCE: UserProjectWorkflowResource = UserProjectWorkflowResource()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord> = com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord::class.java

    /**
     * The column <code>public.user_project_workflow_resource.id</code>.
     */
    val ID: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "")

    /**
     * The column
     * <code>public.user_project_workflow_resource.object_model_uuid</code>.
     */
    val OBJECT_MODEL_UUID: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, String?> = createField(DSL.name("object_model_uuid"), SQLDataType.VARCHAR(255).nullable(false), this, "")

    /**
     * The column
     * <code>public.user_project_workflow_resource.creation_timestamp</code>.
     */
    val CREATION_TIMESTAMP: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, Long?> = createField(DSL.name("creation_timestamp"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column
     * <code>public.user_project_workflow_resource.updated_timestamp</code>.
     */
    val UPDATED_TIMESTAMP: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, Long?> = createField(DSL.name("updated_timestamp"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column
     * <code>public.user_project_workflow_resource.workflow_id</code>.
     */
    val WORKFLOW_ID: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, Long?> = createField(DSL.name("workflow_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column
     * <code>public.user_project_workflow_resource.resource_type</code>.
     */
    val RESOURCE_TYPE: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, String?> = createField(DSL.name("resource_type"), SQLDataType.VARCHAR(511).nullable(false), this, "")

    /**
     * The column
     * <code>public.user_project_workflow_resource.parent_resource_id</code>.
     */
    val PARENT_RESOURCE_ID: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, Long?> = createField(DSL.name("parent_resource_id"), SQLDataType.BIGINT, this, "")

    /**
     * The column
     * <code>public.user_project_workflow_resource.resource_data</code>.
     */
    val RESOURCE_DATA: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, JSONB?> = createField(DSL.name("resource_data"), SQLDataType.JSONB.nullable(false), this, "")

    /**
     * The column <code>public.user_project_workflow_resource.ordinal</code>.
     */
    val ORDINAL: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, Int?> = createField(DSL.name("ordinal"), SQLDataType.INTEGER.nullable(false), this, "")

    /**
     * The column <code>public.user_project_workflow_resource.is_valid</code>.
     */
    val IS_VALID: TableField<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, Boolean?> = createField(DSL.name("is_valid"), SQLDataType.BOOLEAN.nullable(false), this, "")

    private constructor(alias: Name, aliased: Table<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>public.user_project_workflow_resource</code>
     * table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>public.user_project_workflow_resource</code>
     * table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>public.user_project_workflow_resource</code> table
     * reference
     */
    constructor(): this(DSL.name("user_project_workflow_resource"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord>): this(Internal.createPathAlias(child, key), child, key, USER_PROJECT_WORKFLOW_RESOURCE, null)
    override fun getSchema(): Schema? = if (aliased()) null else com.arbr.db.`public`.Public.PUBLIC
    override fun getIndexes(): List<Index> = listOf(com.arbr.db.`public`.indexes.USER_PROJECT_WORKFLOW_RESOURCE_UUID_IDX, com.arbr.db.`public`.indexes.USER_PROJECT_WORKFLOW_RESOURCE_WORKFLOW_ID_IDX)
    override fun getIdentity(): Identity<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, Long?> = super.getIdentity() as Identity<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, Long?>
    override fun getPrimaryKey(): UniqueKey<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord> = com.arbr.db.`public`.keys.USER_PROJECT_WORKFLOW_RESOURCE_PKEY
    override fun getUniqueKeys(): List<UniqueKey<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord>> = listOf(com.arbr.db.`public`.keys.USER_PROJECT_WORKFLOW_RESOURCE_OBJECT_MODEL_UUID_KEY)
    override fun getReferences(): List<ForeignKey<com.arbr.db.`public`.tables.records.UserProjectWorkflowResourceRecord, *>> = listOf(com.arbr.db.`public`.keys.USER_PROJECT_WORKFLOW_RESOURCE__USER_PROJECT_WORKFLOW_RESOURCE_WORKFLOW_ID_FKEY, com.arbr.db.`public`.keys.USER_PROJECT_WORKFLOW_RESOURCE__USER_PROJECT_WORKFLOW_RESOURCE_PARENT_RESOURCE_ID_FKEY)

    private lateinit var _userProjectWorkflow: com.arbr.db.`public`.tables.UserProjectWorkflow
    private lateinit var _userProjectWorkflowResource: com.arbr.db.`public`.tables.UserProjectWorkflowResource

    /**
     * Get the implicit join path to the
     * <code>public.user_project_workflow</code> table.
     */
    fun userProjectWorkflow(): com.arbr.db.`public`.tables.UserProjectWorkflow {
        if (!this::_userProjectWorkflow.isInitialized)
            _userProjectWorkflow = com.arbr.db.`public`.tables.UserProjectWorkflow(this, com.arbr.db.`public`.keys.USER_PROJECT_WORKFLOW_RESOURCE__USER_PROJECT_WORKFLOW_RESOURCE_WORKFLOW_ID_FKEY)

        return _userProjectWorkflow;
    }

    val userProjectWorkflow: com.arbr.db.`public`.tables.UserProjectWorkflow
        get(): com.arbr.db.`public`.tables.UserProjectWorkflow = userProjectWorkflow()

    /**
     * Get the implicit join path to the
     * <code>public.user_project_workflow_resource</code> table.
     */
    fun userProjectWorkflowResource(): com.arbr.db.`public`.tables.UserProjectWorkflowResource {
        if (!this::_userProjectWorkflowResource.isInitialized)
            _userProjectWorkflowResource = com.arbr.db.`public`.tables.UserProjectWorkflowResource(this, com.arbr.db.`public`.keys.USER_PROJECT_WORKFLOW_RESOURCE__USER_PROJECT_WORKFLOW_RESOURCE_PARENT_RESOURCE_ID_FKEY)

        return _userProjectWorkflowResource;
    }

    val userProjectWorkflowResource: com.arbr.db.`public`.tables.UserProjectWorkflowResource
        get(): com.arbr.db.`public`.tables.UserProjectWorkflowResource = userProjectWorkflowResource()
    override fun `as`(alias: String): UserProjectWorkflowResource = UserProjectWorkflowResource(DSL.name(alias), this)
    override fun `as`(alias: Name): UserProjectWorkflowResource = UserProjectWorkflowResource(alias, this)
    override fun `as`(alias: Table<*>): UserProjectWorkflowResource = UserProjectWorkflowResource(alias.getQualifiedName(), this)

    /**
     * Rename this table
     */
    override fun rename(name: String): UserProjectWorkflowResource = UserProjectWorkflowResource(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): UserProjectWorkflowResource = UserProjectWorkflowResource(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): UserProjectWorkflowResource = UserProjectWorkflowResource(name.getQualifiedName(), null)

    // -------------------------------------------------------------------------
    // Row10 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row10<Long?, String?, Long?, Long?, Long?, String?, Long?, JSONB?, Int?, Boolean?> = super.fieldsRow() as Row10<Long?, String?, Long?, Long?, Long?, String?, Long?, JSONB?, Int?, Boolean?>

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    fun <U> mapping(from: (Long?, String?, Long?, Long?, Long?, String?, Long?, JSONB?, Int?, Boolean?) -> U): SelectField<U> = convertFrom(Records.mapping(from))

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    fun <U> mapping(toType: Class<U>, from: (Long?, String?, Long?, Long?, Long?, String?, Long?, JSONB?, Int?, Boolean?) -> U): SelectField<U> = convertFrom(toType, Records.mapping(from))
}