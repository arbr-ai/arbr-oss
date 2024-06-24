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
open class EmbeddedContent(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, com.arbr.db.`public`.tables.records.EmbeddedContentRecord>?,
    aliased: Table<com.arbr.db.`public`.tables.records.EmbeddedContentRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<com.arbr.db.`public`.tables.records.EmbeddedContentRecord>(
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
         * The reference instance of <code>public.embedded_content</code>
         */
        val EMBEDDED_CONTENT: EmbeddedContent = EmbeddedContent()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<com.arbr.db.`public`.tables.records.EmbeddedContentRecord> = com.arbr.db.`public`.tables.records.EmbeddedContentRecord::class.java

    /**
     * The column <code>public.embedded_content.id</code>.
     */
    val ID: TableField<com.arbr.db.`public`.tables.records.EmbeddedContentRecord, Long?> = createField(DSL.name("id"), SQLDataType.BIGINT.nullable(false).identity(true), this, "")

    /**
     * The column <code>public.embedded_content.creation_timestamp</code>.
     */
    val CREATION_TIMESTAMP: TableField<com.arbr.db.`public`.tables.records.EmbeddedContentRecord, Long?> = createField(DSL.name("creation_timestamp"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>public.embedded_content.resource_id</code>.
     */
    val RESOURCE_ID: TableField<com.arbr.db.`public`.tables.records.EmbeddedContentRecord, Long?> = createField(DSL.name("resource_id"), SQLDataType.BIGINT.nullable(false), this, "")

    /**
     * The column <code>public.embedded_content.vector_id</code>.
     */
    val VECTOR_ID: TableField<com.arbr.db.`public`.tables.records.EmbeddedContentRecord, String?> = createField(DSL.name("vector_id"), SQLDataType.VARCHAR(511).nullable(false), this, "")

    /**
     * The column <code>public.embedded_content.schema_id</code>.
     */
    val SCHEMA_ID: TableField<com.arbr.db.`public`.tables.records.EmbeddedContentRecord, String?> = createField(DSL.name("schema_id"), SQLDataType.VARCHAR(511).nullable(false), this, "")

    /**
     * The column <code>public.embedded_content.kind</code>.
     */
    val KIND: TableField<com.arbr.db.`public`.tables.records.EmbeddedContentRecord, String?> = createField(DSL.name("kind"), SQLDataType.VARCHAR(511).nullable(false), this, "")

    /**
     * The column <code>public.embedded_content.embedding_content</code>.
     */
    val EMBEDDING_CONTENT: TableField<com.arbr.db.`public`.tables.records.EmbeddedContentRecord, String?> = createField(DSL.name("embedding_content"), SQLDataType.VARCHAR(65535).nullable(false), this, "")

    /**
     * The column <code>public.embedded_content.metadata</code>.
     */
    val METADATA: TableField<com.arbr.db.`public`.tables.records.EmbeddedContentRecord, JSONB?> = createField(DSL.name("metadata"), SQLDataType.JSONB, this, "")

    private constructor(alias: Name, aliased: Table<com.arbr.db.`public`.tables.records.EmbeddedContentRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<com.arbr.db.`public`.tables.records.EmbeddedContentRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>public.embedded_content</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>public.embedded_content</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>public.embedded_content</code> table reference
     */
    constructor(): this(DSL.name("embedded_content"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, com.arbr.db.`public`.tables.records.EmbeddedContentRecord>): this(Internal.createPathAlias(child, key), child, key, EMBEDDED_CONTENT, null)
    override fun getSchema(): Schema? = if (aliased()) null else com.arbr.db.`public`.Public.PUBLIC
    override fun getIndexes(): List<Index> = listOf(com.arbr.db.`public`.indexes.EMBEDDED_CONTENT_VECTOR_ID)
    override fun getIdentity(): Identity<com.arbr.db.`public`.tables.records.EmbeddedContentRecord, Long?> = super.getIdentity() as Identity<com.arbr.db.`public`.tables.records.EmbeddedContentRecord, Long?>
    override fun getPrimaryKey(): UniqueKey<com.arbr.db.`public`.tables.records.EmbeddedContentRecord> = com.arbr.db.`public`.keys.EMBEDDED_CONTENT_PKEY
    override fun getReferences(): List<ForeignKey<com.arbr.db.`public`.tables.records.EmbeddedContentRecord, *>> = listOf(com.arbr.db.`public`.keys.EMBEDDED_CONTENT__EMBEDDED_CONTENT_RESOURCE_ID_FKEY)

    private lateinit var _indexedResource: com.arbr.db.`public`.tables.IndexedResource

    /**
     * Get the implicit join path to the <code>public.indexed_resource</code>
     * table.
     */
    fun indexedResource(): com.arbr.db.`public`.tables.IndexedResource {
        if (!this::_indexedResource.isInitialized)
            _indexedResource = com.arbr.db.`public`.tables.IndexedResource(this, com.arbr.db.`public`.keys.EMBEDDED_CONTENT__EMBEDDED_CONTENT_RESOURCE_ID_FKEY)

        return _indexedResource;
    }

    val indexedResource: com.arbr.db.`public`.tables.IndexedResource
        get(): com.arbr.db.`public`.tables.IndexedResource = indexedResource()
    override fun `as`(alias: String): EmbeddedContent = EmbeddedContent(DSL.name(alias), this)
    override fun `as`(alias: Name): EmbeddedContent = EmbeddedContent(alias, this)
    override fun `as`(alias: Table<*>): EmbeddedContent = EmbeddedContent(alias.getQualifiedName(), this)

    /**
     * Rename this table
     */
    override fun rename(name: String): EmbeddedContent = EmbeddedContent(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): EmbeddedContent = EmbeddedContent(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): EmbeddedContent = EmbeddedContent(name.getQualifiedName(), null)

    // -------------------------------------------------------------------------
    // Row8 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row8<Long?, Long?, Long?, String?, String?, String?, String?, JSONB?> = super.fieldsRow() as Row8<Long?, Long?, Long?, String?, String?, String?, String?, JSONB?>

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    fun <U> mapping(from: (Long?, Long?, Long?, String?, String?, String?, String?, JSONB?) -> U): SelectField<U> = convertFrom(Records.mapping(from))

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    fun <U> mapping(toType: Class<U>, from: (Long?, Long?, Long?, String?, String?, String?, String?, JSONB?) -> U): SelectField<U> = convertFrom(toType, Records.mapping(from))
}
