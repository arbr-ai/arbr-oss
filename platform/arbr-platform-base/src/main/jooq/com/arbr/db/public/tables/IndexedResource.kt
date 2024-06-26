/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables


import java.util.function.Function

import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Identity
import org.jooq.JSONB
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
open class IndexedResource(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, com.arbr.db.`public`.tables.records.IndexedResourceRecord>?,
    aliased: Table<com.arbr.db.`public`.tables.records.IndexedResourceRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<com.arbr.db.`public`.tables.records.IndexedResourceRecord>(
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
         * The reference instance of <code>public.indexed_resource</code>
         */
        val INDEXED_RESOURCE: IndexedResource = IndexedResource()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<com.arbr.db.`public`.tables.records.IndexedResourceRecord> = com.arbr.db.`public`.tables.records.IndexedResourceRecord::class.java

    /**
     * The column <code>public.indexed_resource.id</code>.
     */
    val ID: TableField<com.arbr.db.`public`.tables.records.IndexedResourceRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "")

    /**
     * The column <code>public.indexed_resource.creation_timestamp</code>.
     */
    val CREATION_TIMESTAMP: TableField<com.arbr.db.`public`.tables.records.IndexedResourceRecord, Long?> = createField(DSL.name("creation_timestamp"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>public.indexed_resource.schema_id</code>.
     */
    val SCHEMA_ID: TableField<com.arbr.db.`public`.tables.records.IndexedResourceRecord, String?> = createField(DSL.name("schema_id"), SQLDataType.VARCHAR(511).nullable(false), this, "")

    /**
     * The column <code>public.indexed_resource.resource_object</code>.
     */
    val RESOURCE_OBJECT: TableField<com.arbr.db.`public`.tables.records.IndexedResourceRecord, JSONB?> = createField(DSL.name("resource_object"), SQLDataType.JSONB.nullable(false), this, "")

    /**
     * The column <code>public.indexed_resource.chat_messages</code>.
     */
    val CHAT_MESSAGES: TableField<com.arbr.db.`public`.tables.records.IndexedResourceRecord, JSONB?> = createField(DSL.name("chat_messages"), SQLDataType.JSONB, this, "")

    private constructor(alias: Name, aliased: Table<com.arbr.db.`public`.tables.records.IndexedResourceRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<com.arbr.db.`public`.tables.records.IndexedResourceRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>public.indexed_resource</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>public.indexed_resource</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>public.indexed_resource</code> table reference
     */
    constructor(): this(DSL.name("indexed_resource"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, com.arbr.db.`public`.tables.records.IndexedResourceRecord>): this(Internal.createPathAlias(child, key), child, key, INDEXED_RESOURCE, null)
    override fun getSchema(): Schema? = if (aliased()) null else com.arbr.db.`public`.Public.PUBLIC
    override fun getIdentity(): Identity<com.arbr.db.`public`.tables.records.IndexedResourceRecord, Long?> = super.getIdentity() as Identity<com.arbr.db.`public`.tables.records.IndexedResourceRecord, Long?>
    override fun getPrimaryKey(): UniqueKey<com.arbr.db.`public`.tables.records.IndexedResourceRecord> = com.arbr.db.`public`.keys.INDEXED_RESOURCE_PKEY
    override fun `as`(alias: String): IndexedResource = IndexedResource(DSL.name(alias), this)
    override fun `as`(alias: Name): IndexedResource = IndexedResource(alias, this)
    override fun `as`(alias: Table<*>): IndexedResource = IndexedResource(alias.getQualifiedName(), this)

    /**
     * Rename this table
     */
    override fun rename(name: String): IndexedResource = IndexedResource(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): IndexedResource = IndexedResource(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): IndexedResource = IndexedResource(name.getQualifiedName(), null)

    // -------------------------------------------------------------------------
    // Row5 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row5<Long?, Long?, String?, JSONB?, JSONB?> = super.fieldsRow() as Row5<Long?, Long?, String?, JSONB?, JSONB?>

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    fun <U> mapping(from: (Long?, Long?, String?, JSONB?, JSONB?) -> U): SelectField<U> = convertFrom(Records.mapping(from))

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    fun <U> mapping(toType: Class<U>, from: (Long?, Long?, String?, JSONB?, JSONB?) -> U): SelectField<U> = convertFrom(toType, Records.mapping(from))
}
