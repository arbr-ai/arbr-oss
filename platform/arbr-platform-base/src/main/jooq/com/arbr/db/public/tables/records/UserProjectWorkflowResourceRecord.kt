/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables.records


import org.jooq.Field
import org.jooq.JSONB
import org.jooq.Record1
import org.jooq.Record10
import org.jooq.Row10
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class UserProjectWorkflowResourceRecord private constructor() : UpdatableRecordImpl<UserProjectWorkflowResourceRecord>(com.arbr.db.`public`.tables.UserProjectWorkflowResource.USER_PROJECT_WORKFLOW_RESOURCE), Record10<Long?, String?, Long?, Long?, Long?, String?, Long?, JSONB?, Int?, Boolean?> {

    open var id: Long?
        set(value): Unit = set(0, value)
        get(): Long? = get(0) as Long?

    open var objectModelUuid: String
        set(value): Unit = set(1, value)
        get(): String = get(1) as String

    open var creationTimestamp: Long
        set(value): Unit = set(2, value)
        get(): Long = get(2) as Long

    open var updatedTimestamp: Long
        set(value): Unit = set(3, value)
        get(): Long = get(3) as Long

    open var workflowId: Long
        set(value): Unit = set(4, value)
        get(): Long = get(4) as Long

    open var resourceType: String
        set(value): Unit = set(5, value)
        get(): String = get(5) as String

    open var parentResourceId: Long?
        set(value): Unit = set(6, value)
        get(): Long? = get(6) as Long?

    open var resourceData: JSONB
        set(value): Unit = set(7, value)
        get(): JSONB = get(7) as JSONB

    open var ordinal: Int
        set(value): Unit = set(8, value)
        get(): Int = get(8) as Int

    @Suppress("INAPPLICABLE_JVM_NAME")
    @set:JvmName("setIsValid")
    open var isValid: Boolean
        set(value): Unit = set(9, value)
        get(): Boolean = get(9) as Boolean

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<Long?> = super.key() as Record1<Long?>

    // -------------------------------------------------------------------------
    // Record10 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row10<Long?, String?, Long?, Long?, Long?, String?, Long?, JSONB?, Int?, Boolean?> = super.fieldsRow() as Row10<Long?, String?, Long?, Long?, Long?, String?, Long?, JSONB?, Int?, Boolean?>
    override fun valuesRow(): Row10<Long?, String?, Long?, Long?, Long?, String?, Long?, JSONB?, Int?, Boolean?> = super.valuesRow() as Row10<Long?, String?, Long?, Long?, Long?, String?, Long?, JSONB?, Int?, Boolean?>
    override fun field1(): Field<Long?> = com.arbr.db.`public`.tables.UserProjectWorkflowResource.USER_PROJECT_WORKFLOW_RESOURCE.ID
    override fun field2(): Field<String?> = com.arbr.db.`public`.tables.UserProjectWorkflowResource.USER_PROJECT_WORKFLOW_RESOURCE.OBJECT_MODEL_UUID
    override fun field3(): Field<Long?> = com.arbr.db.`public`.tables.UserProjectWorkflowResource.USER_PROJECT_WORKFLOW_RESOURCE.CREATION_TIMESTAMP
    override fun field4(): Field<Long?> = com.arbr.db.`public`.tables.UserProjectWorkflowResource.USER_PROJECT_WORKFLOW_RESOURCE.UPDATED_TIMESTAMP
    override fun field5(): Field<Long?> = com.arbr.db.`public`.tables.UserProjectWorkflowResource.USER_PROJECT_WORKFLOW_RESOURCE.WORKFLOW_ID
    override fun field6(): Field<String?> = com.arbr.db.`public`.tables.UserProjectWorkflowResource.USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_TYPE
    override fun field7(): Field<Long?> = com.arbr.db.`public`.tables.UserProjectWorkflowResource.USER_PROJECT_WORKFLOW_RESOURCE.PARENT_RESOURCE_ID
    override fun field8(): Field<JSONB?> = com.arbr.db.`public`.tables.UserProjectWorkflowResource.USER_PROJECT_WORKFLOW_RESOURCE.RESOURCE_DATA
    override fun field9(): Field<Int?> = com.arbr.db.`public`.tables.UserProjectWorkflowResource.USER_PROJECT_WORKFLOW_RESOURCE.ORDINAL
    override fun field10(): Field<Boolean?> = com.arbr.db.`public`.tables.UserProjectWorkflowResource.USER_PROJECT_WORKFLOW_RESOURCE.IS_VALID
    override fun component1(): Long? = id
    override fun component2(): String = objectModelUuid
    override fun component3(): Long = creationTimestamp
    override fun component4(): Long = updatedTimestamp
    override fun component5(): Long = workflowId
    override fun component6(): String = resourceType
    override fun component7(): Long? = parentResourceId
    override fun component8(): JSONB = resourceData
    override fun component9(): Int = ordinal
    override fun component10(): Boolean = isValid
    override fun value1(): Long? = id
    override fun value2(): String = objectModelUuid
    override fun value3(): Long = creationTimestamp
    override fun value4(): Long = updatedTimestamp
    override fun value5(): Long = workflowId
    override fun value6(): String = resourceType
    override fun value7(): Long? = parentResourceId
    override fun value8(): JSONB = resourceData
    override fun value9(): Int = ordinal
    override fun value10(): Boolean = isValid

    override fun value1(value: Long?): UserProjectWorkflowResourceRecord {
        set(0, value)
        return this
    }

    override fun value2(value: String?): UserProjectWorkflowResourceRecord {
        set(1, value)
        return this
    }

    override fun value3(value: Long?): UserProjectWorkflowResourceRecord {
        set(2, value)
        return this
    }

    override fun value4(value: Long?): UserProjectWorkflowResourceRecord {
        set(3, value)
        return this
    }

    override fun value5(value: Long?): UserProjectWorkflowResourceRecord {
        set(4, value)
        return this
    }

    override fun value6(value: String?): UserProjectWorkflowResourceRecord {
        set(5, value)
        return this
    }

    override fun value7(value: Long?): UserProjectWorkflowResourceRecord {
        set(6, value)
        return this
    }

    override fun value8(value: JSONB?): UserProjectWorkflowResourceRecord {
        set(7, value)
        return this
    }

    override fun value9(value: Int?): UserProjectWorkflowResourceRecord {
        set(8, value)
        return this
    }

    override fun value10(value: Boolean?): UserProjectWorkflowResourceRecord {
        set(9, value)
        return this
    }

    override fun values(value1: Long?, value2: String?, value3: Long?, value4: Long?, value5: Long?, value6: String?, value7: Long?, value8: JSONB?, value9: Int?, value10: Boolean?): UserProjectWorkflowResourceRecord {
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
     * Create a detached, initialised UserProjectWorkflowResourceRecord
     */
    constructor(id: Long? = null, objectModelUuid: String, creationTimestamp: Long, updatedTimestamp: Long, workflowId: Long, resourceType: String, parentResourceId: Long? = null, resourceData: JSONB, ordinal: Int, isValid: Boolean): this() {
        this.id = id
        this.objectModelUuid = objectModelUuid
        this.creationTimestamp = creationTimestamp
        this.updatedTimestamp = updatedTimestamp
        this.workflowId = workflowId
        this.resourceType = resourceType
        this.parentResourceId = parentResourceId
        this.resourceData = resourceData
        this.ordinal = ordinal
        this.isValid = isValid
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised UserProjectWorkflowResourceRecord
     */
    constructor(value: com.arbr.db.`public`.tables.pojos.UserProjectWorkflowResource?): this() {
        if (value != null) {
            this.id = value.id
            this.objectModelUuid = value.objectModelUuid
            this.creationTimestamp = value.creationTimestamp
            this.updatedTimestamp = value.updatedTimestamp
            this.workflowId = value.workflowId
            this.resourceType = value.resourceType
            this.parentResourceId = value.parentResourceId
            this.resourceData = value.resourceData
            this.ordinal = value.ordinal
            this.isValid = value.isValid
            resetChangedOnNotNull()
        }
    }
}
