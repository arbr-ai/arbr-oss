/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables.records


import org.jooq.Field
import org.jooq.Record1
import org.jooq.Record6
import org.jooq.Row6
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class UserProjectWorkflowEventStreamRecord private constructor() : UpdatableRecordImpl<UserProjectWorkflowEventStreamRecord>(com.arbr.db.`public`.tables.UserProjectWorkflowEventStream.USER_PROJECT_WORKFLOW_EVENT_STREAM), Record6<Long?, Long?, String?, String?, Long?, Long?> {

    open var id: Long?
        set(value): Unit = set(0, value)
        get(): Long? = get(0) as Long?

    open var workflowId: Long
        set(value): Unit = set(1, value)
        get(): Long = get(1) as Long

    open var topicName: String
        set(value): Unit = set(2, value)
        get(): String = get(2) as String

    open var stateHash: String
        set(value): Unit = set(3, value)
        get(): String = get(3) as String

    open var ordinalOffset: Long
        set(value): Unit = set(4, value)
        get(): Long = get(4) as Long

    open var eventTimestamp: Long
        set(value): Unit = set(5, value)
        get(): Long = get(5) as Long

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<Long?> = super.key() as Record1<Long?>

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row6<Long?, Long?, String?, String?, Long?, Long?> = super.fieldsRow() as Row6<Long?, Long?, String?, String?, Long?, Long?>
    override fun valuesRow(): Row6<Long?, Long?, String?, String?, Long?, Long?> = super.valuesRow() as Row6<Long?, Long?, String?, String?, Long?, Long?>
    override fun field1(): Field<Long?> = com.arbr.db.`public`.tables.UserProjectWorkflowEventStream.USER_PROJECT_WORKFLOW_EVENT_STREAM.ID
    override fun field2(): Field<Long?> = com.arbr.db.`public`.tables.UserProjectWorkflowEventStream.USER_PROJECT_WORKFLOW_EVENT_STREAM.WORKFLOW_ID
    override fun field3(): Field<String?> = com.arbr.db.`public`.tables.UserProjectWorkflowEventStream.USER_PROJECT_WORKFLOW_EVENT_STREAM.TOPIC_NAME
    override fun field4(): Field<String?> = com.arbr.db.`public`.tables.UserProjectWorkflowEventStream.USER_PROJECT_WORKFLOW_EVENT_STREAM.STATE_HASH
    override fun field5(): Field<Long?> = com.arbr.db.`public`.tables.UserProjectWorkflowEventStream.USER_PROJECT_WORKFLOW_EVENT_STREAM.ORDINAL_OFFSET
    override fun field6(): Field<Long?> = com.arbr.db.`public`.tables.UserProjectWorkflowEventStream.USER_PROJECT_WORKFLOW_EVENT_STREAM.EVENT_TIMESTAMP
    override fun component1(): Long? = id
    override fun component2(): Long = workflowId
    override fun component3(): String = topicName
    override fun component4(): String = stateHash
    override fun component5(): Long = ordinalOffset
    override fun component6(): Long = eventTimestamp
    override fun value1(): Long? = id
    override fun value2(): Long = workflowId
    override fun value3(): String = topicName
    override fun value4(): String = stateHash
    override fun value5(): Long = ordinalOffset
    override fun value6(): Long = eventTimestamp

    override fun value1(value: Long?): UserProjectWorkflowEventStreamRecord {
        set(0, value)
        return this
    }

    override fun value2(value: Long?): UserProjectWorkflowEventStreamRecord {
        set(1, value)
        return this
    }

    override fun value3(value: String?): UserProjectWorkflowEventStreamRecord {
        set(2, value)
        return this
    }

    override fun value4(value: String?): UserProjectWorkflowEventStreamRecord {
        set(3, value)
        return this
    }

    override fun value5(value: Long?): UserProjectWorkflowEventStreamRecord {
        set(4, value)
        return this
    }

    override fun value6(value: Long?): UserProjectWorkflowEventStreamRecord {
        set(5, value)
        return this
    }

    override fun values(value1: Long?, value2: Long?, value3: String?, value4: String?, value5: Long?, value6: Long?): UserProjectWorkflowEventStreamRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        this.value5(value5)
        this.value6(value6)
        return this
    }

    /**
     * Create a detached, initialised UserProjectWorkflowEventStreamRecord
     */
    constructor(id: Long? = null, workflowId: Long, topicName: String, stateHash: String, ordinalOffset: Long, eventTimestamp: Long): this() {
        this.id = id
        this.workflowId = workflowId
        this.topicName = topicName
        this.stateHash = stateHash
        this.ordinalOffset = ordinalOffset
        this.eventTimestamp = eventTimestamp
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised UserProjectWorkflowEventStreamRecord
     */
    constructor(value: com.arbr.db.`public`.tables.pojos.UserProjectWorkflowEventStream?): this() {
        if (value != null) {
            this.id = value.id
            this.workflowId = value.workflowId
            this.topicName = value.topicName
            this.stateHash = value.stateHash
            this.ordinalOffset = value.ordinalOffset
            this.eventTimestamp = value.eventTimestamp
            resetChangedOnNotNull()
        }
    }
}
