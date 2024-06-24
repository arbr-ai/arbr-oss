/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.tables.records


import com.arbr.db.binding.Vector1536

import org.jooq.Field
import org.jooq.Record1
import org.jooq.Record6
import org.jooq.Row6
import org.jooq.impl.UpdatableRecordImpl


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class VectorEmbeddingRecord private constructor() : UpdatableRecordImpl<VectorEmbeddingRecord>(com.arbr.db.`public`.tables.VectorEmbedding.VECTOR_EMBEDDING), Record6<String?, String?, String?, String?, String?, Vector1536?> {

    open var vectorId: String
        set(value): Unit = set(0, value)
        get(): String = get(0) as String

    open var namespace: String
        set(value): Unit = set(1, value)
        get(): String = get(1) as String

    open var versionId: String
        set(value): Unit = set(2, value)
        get(): String = get(2) as String

    open var schemaId: String
        set(value): Unit = set(3, value)
        get(): String = get(3) as String

    open var embeddingContent: String
        set(value): Unit = set(4, value)
        get(): String = get(4) as String

    open var embedding: Vector1536
        set(value): Unit = set(5, value)
        get(): Vector1536 = get(5) as Vector1536

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    override fun key(): Record1<String?> = super.key() as Record1<String?>

    // -------------------------------------------------------------------------
    // Record6 type implementation
    // -------------------------------------------------------------------------

    override fun fieldsRow(): Row6<String?, String?, String?, String?, String?, Vector1536?> = super.fieldsRow() as Row6<String?, String?, String?, String?, String?, Vector1536?>
    override fun valuesRow(): Row6<String?, String?, String?, String?, String?, Vector1536?> = super.valuesRow() as Row6<String?, String?, String?, String?, String?, Vector1536?>
    override fun field1(): Field<String?> = com.arbr.db.`public`.tables.VectorEmbedding.VECTOR_EMBEDDING.VECTOR_ID
    override fun field2(): Field<String?> = com.arbr.db.`public`.tables.VectorEmbedding.VECTOR_EMBEDDING.NAMESPACE
    override fun field3(): Field<String?> = com.arbr.db.`public`.tables.VectorEmbedding.VECTOR_EMBEDDING.VERSION_ID
    override fun field4(): Field<String?> = com.arbr.db.`public`.tables.VectorEmbedding.VECTOR_EMBEDDING.SCHEMA_ID
    override fun field5(): Field<String?> = com.arbr.db.`public`.tables.VectorEmbedding.VECTOR_EMBEDDING.EMBEDDING_CONTENT
    override fun field6(): Field<Vector1536?> = com.arbr.db.`public`.tables.VectorEmbedding.VECTOR_EMBEDDING.EMBEDDING
    override fun component1(): String = vectorId
    override fun component2(): String = namespace
    override fun component3(): String = versionId
    override fun component4(): String = schemaId
    override fun component5(): String = embeddingContent
    override fun component6(): Vector1536 = embedding
    override fun value1(): String = vectorId
    override fun value2(): String = namespace
    override fun value3(): String = versionId
    override fun value4(): String = schemaId
    override fun value5(): String = embeddingContent
    override fun value6(): Vector1536 = embedding

    override fun value1(value: String?): VectorEmbeddingRecord {
        set(0, value)
        return this
    }

    override fun value2(value: String?): VectorEmbeddingRecord {
        set(1, value)
        return this
    }

    override fun value3(value: String?): VectorEmbeddingRecord {
        set(2, value)
        return this
    }

    override fun value4(value: String?): VectorEmbeddingRecord {
        set(3, value)
        return this
    }

    override fun value5(value: String?): VectorEmbeddingRecord {
        set(4, value)
        return this
    }

    override fun value6(value: Vector1536?): VectorEmbeddingRecord {
        set(5, value)
        return this
    }

    override fun values(value1: String?, value2: String?, value3: String?, value4: String?, value5: String?, value6: Vector1536?): VectorEmbeddingRecord {
        this.value1(value1)
        this.value2(value2)
        this.value3(value3)
        this.value4(value4)
        this.value5(value5)
        this.value6(value6)
        return this
    }

    /**
     * Create a detached, initialised VectorEmbeddingRecord
     */
    constructor(vectorId: String, namespace: String, versionId: String, schemaId: String, embeddingContent: String, embedding: Vector1536): this() {
        this.vectorId = vectorId
        this.namespace = namespace
        this.versionId = versionId
        this.schemaId = schemaId
        this.embeddingContent = embeddingContent
        this.embedding = embedding
        resetChangedOnNotNull()
    }

    /**
     * Create a detached, initialised VectorEmbeddingRecord
     */
    constructor(value: com.arbr.db.`public`.tables.pojos.VectorEmbedding?): this() {
        if (value != null) {
            this.vectorId = value.vectorId
            this.namespace = value.namespace
            this.versionId = value.versionId
            this.schemaId = value.schemaId
            this.embeddingContent = value.embeddingContent
            this.embedding = value.embedding
            resetChangedOnNotNull()
        }
    }
}
