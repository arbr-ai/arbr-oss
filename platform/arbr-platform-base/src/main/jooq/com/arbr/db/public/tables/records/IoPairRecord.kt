/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables.records


import org.jooq.Field
import org.jooq.Record4
import org.jooq.Row4
import org.jooq.impl.TableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class IoPairRecord private constructor() : TableRecordImpl<IoPairRecord>(com.arbr.db.`public`.tables.IoPair.IO_PAIR), Record4<Long?, Long?, Long?, Long?> {

    open var id: Long?
        set(value): Unit = set(0, value)
        get(): Long? = get(0) as Long?

    open var creationTimestamp: Long
        set(value): Unit = set(1, value)
        get(): Long = get(1) as Long

    open var inputResourceId: Long
        set(value): Unit = set(2, value)
        get(): Long = get(2) as Long

    open var outputResourceId: Long
        set(value): Unit = set(3, value)
        get(): Long = get(3) as Long

    // -------------------------------------------------------------------------
    // Record4 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row4<Long?, Long?, Long?, Long?> = super.fieldsRow() as Row4<Long?, Long?, Long?, Long?>
    override fun valuesRow(): Row4<Long?, Long?, Long?, Long?> = super.valuesRow() as Row4<Long?, Long?, Long?, Long?>
    override fun field1(): Field<Long?> = com.arbr.db.`public`.tables.IoPair.IO_PAIR.ID
    override fun field2(): Field<Long?> = com.arbr.db.`public`.tables.IoPair.IO_PAIR.CREATION_TIMESTAMP
    override fun field3(): Field<Long?> = com.arbr.db.`public`.tables.IoPair.IO_PAIR.INPUT_RESOURCE_ID
    override fun field4(): Field<Long?> = com.arbr.db.`public`.tables.IoPair.IO_PAIR.OUTPUT_RESOURCE_ID
    override fun component1(): Long? = id
    override fun component2(): Long = creationTimestamp
    override fun component3(): Long = inputResourceId
    override fun component4(): Long = outputResourceId
    override fun value1(): Long? = id
    override fun value2(): Long = creationTimestamp
    override fun value3(): Long = inputResourceId
    override fun value4(): Long = outputResourceId

    override fun value1(value: Long?): IoPairRecord {
        set(0, value)
        return this
    }

    override fun value2(value: Long?): IoPairRecord {
        set(1, value)
        return this
    }

    override fun value3(value: Long?): IoPairRecord {
        set(2, value)
        return this
    }

    override fun value4(value: Long?): IoPairRecord {
        set(3, value)
        return this
    }

    override fun values(value1: Long?, value2: Long?, value3: Long?, value4: Long?): IoPairRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        return this
    }

    /**
     * Create a detached, initialised IoPairRecord
     */
    constructor(id: Long? = null, creationTimestamp: Long, inputResourceId: Long, outputResourceId: Long): this() {
        this.id = id
        this.creationTimestamp = creationTimestamp
        this.inputResourceId = inputResourceId
        this.outputResourceId = outputResourceId
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised IoPairRecord
     */
    constructor(value: com.arbr.db.`public`.tables.pojos.IoPair?): this() {
        if (value != null) {
            this.id = value.id
            this.creationTimestamp = value.creationTimestamp
            this.inputResourceId = value.inputResourceId
            this.outputResourceId = value.outputResourceId
            resetChangedOnNotNull()
        }
    }
}
