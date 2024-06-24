/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables


import java.util.function.Function

import org.jooq.Field
import org.jooq.ForeignKey
import org.jooq.Name
import org.jooq.Record
import org.jooq.Records
import org.jooq.Row1
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
open class AlphaCode(
    alias: Name,
    child: Table<out Record>?,
    path: ForeignKey<out Record, com.arbr.db.`public`.tables.records.AlphaCodeRecord>?,
    aliased: Table<com.arbr.db.`public`.tables.records.AlphaCodeRecord>?,
    parameters: Array<Field<*>?>?
): TableImpl<com.arbr.db.`public`.tables.records.AlphaCodeRecord>(
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
         * The reference instance of <code>public.alpha_code</code>
         */
        val ALPHA_CODE: AlphaCode = AlphaCode()
    }

    /**
     * The class holding records for this type
     */
    override fun getRecordType(): Class<com.arbr.db.`public`.tables.records.AlphaCodeRecord> = com.arbr.db.`public`.tables.records.AlphaCodeRecord::class.java

    /**
     * The column <code>public.alpha_code.code</code>.
     */
    val CODE: TableField<com.arbr.db.`public`.tables.records.AlphaCodeRecord, String?> = createField(DSL.name("code"), SQLDataType.VARCHAR(255).nullable(false), this, "")

    private constructor(alias: Name, aliased: Table<com.arbr.db.`public`.tables.records.AlphaCodeRecord>?): this(alias, null, null, aliased, null)
    private constructor(alias: Name, aliased: Table<com.arbr.db.`public`.tables.records.AlphaCodeRecord>?, parameters: Array<Field<*>?>?): this(alias, null, null, aliased, parameters)

    /**
     * Create an aliased <code>public.alpha_code</code> table reference
     */
    constructor(alias: String): this(DSL.name(alias))

    /**
     * Create an aliased <code>public.alpha_code</code> table reference
     */
    constructor(alias: Name): this(alias, null)

    /**
     * Create a <code>public.alpha_code</code> table reference
     */
    constructor(): this(DSL.name("alpha_code"), null)

    constructor(child: Table<out Record>, key: ForeignKey<out Record, com.arbr.db.`public`.tables.records.AlphaCodeRecord>): this(Internal.createPathAlias(child, key), child, key, ALPHA_CODE, null)
    override fun getSchema(): Schema? = if (aliased()) null else com.arbr.db.`public`.Public.PUBLIC
    override fun getPrimaryKey(): UniqueKey<com.arbr.db.`public`.tables.records.AlphaCodeRecord> = com.arbr.db.`public`.keys.ALPHA_CODE_PKEY
    override fun `as`(alias: String): AlphaCode = AlphaCode(DSL.name(alias), this)
    override fun `as`(alias: Name): AlphaCode = AlphaCode(alias, this)
    override fun `as`(alias: Table<*>): AlphaCode = AlphaCode(alias.getQualifiedName(), this)

    /**
     * Rename this table
     */
    override fun rename(name: String): AlphaCode = AlphaCode(DSL.name(name), null)

    /**
     * Rename this table
     */
    override fun rename(name: Name): AlphaCode = AlphaCode(name, null)

    /**
     * Rename this table
     */
    override fun rename(name: Table<*>): AlphaCode = AlphaCode(name.getQualifiedName(), null)

    // -------------------------------------------------------------------------
    // Row1 type methods
    // -------------------------------------------------------------------------
    override fun fieldsRow(): Row1<String?> = super.fieldsRow() as Row1<String?>

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Function)}.
     */
    fun <U> mapping(from: (String?) -> U): SelectField<U> = convertFrom(Records.mapping(from))

    /**
     * Convenience mapping calling {@link SelectField#convertFrom(Class,
     * Function)}.
     */
    fun <U> mapping(toType: Class<U>, from: (String?) -> U): SelectField<U> = convertFrom(toType, Records.mapping(from))
}
