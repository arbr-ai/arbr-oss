package com.arbr.engine.services.db.client

import com.arbr.content_formats.jsonb.stripNulls
import com.fasterxml.jackson.databind.ObjectMapper
import com.arbr.db.public.tables.pojos.IndexedResource
import com.arbr.db.public.tables.records.IndexedResourceRecord
import com.arbr.db.public.tables.references.INDEXED_RESOURCE
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.asterisk
import org.jooq.impl.DSL.value
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class IndexedResourceStore(
    private val dslContext: DSLContext,
    private val mapper: ObjectMapper,
) {
    fun insert(
        indexedResource: IndexedResourceRecord,
    ): Mono<IndexedResource> {
        return Flux.from(
            dslContext
                .insertInto(
                    INDEXED_RESOURCE,
                    INDEXED_RESOURCE.CREATION_TIMESTAMP,
                    INDEXED_RESOURCE.SCHEMA_ID,
                    INDEXED_RESOURCE.RESOURCE_OBJECT,
                    INDEXED_RESOURCE.CHAT_MESSAGES,
                )
                .values(
                    indexedResource.creationTimestamp,
                    indexedResource.schemaId,
                    indexedResource.resourceObject.stripNulls(),
                    indexedResource.chatMessages?.stripNulls(),
                )
                .returning(asterisk())
        ).map { record: Record ->
            record.into(IndexedResource::class.java)
        }
            .doOnError {
                logger.error("Failed to write indexed resource: \n" + mapper.writeValueAsString(indexedResource))
            }
            .next()
    }

    fun update(
        indexedResource: IndexedResourceRecord,
    ): Mono<IndexedResource> {
        return Flux.from(
            dslContext
                .insertInto(
                    INDEXED_RESOURCE,
                    INDEXED_RESOURCE.ID,
                    INDEXED_RESOURCE.CREATION_TIMESTAMP,
                    INDEXED_RESOURCE.SCHEMA_ID,
                    INDEXED_RESOURCE.RESOURCE_OBJECT,
                    INDEXED_RESOURCE.CHAT_MESSAGES,
                )
                .values(indexedResource)
                .onConflict()
                .doUpdate()
                .set(indexedResource)
                .returning(asterisk())
        ).map { record: Record ->
            record.into(IndexedResource::class.java)
        }.next()
    }

    fun get(
        id: Long,
    ): Mono<IndexedResource> {
        return Flux.from(
            dslContext
                .select(asterisk())
                .from(INDEXED_RESOURCE)
                .where(INDEXED_RESOURCE.ID.eq(value(id)))
        ).map { record: Record ->
            record.into(IndexedResource::class.java)
        }.next()
    }

    fun delete(
        id: Long,
    ): Mono<IndexedResource> {
        return Flux.from(
            dslContext
                .deleteFrom(INDEXED_RESOURCE)
                .where(INDEXED_RESOURCE.ID.eq(value(id)))
                .returning(asterisk())
        )
            .map { record: Record ->
                record.into(IndexedResource::class.java)
            }.next()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(IndexedResourceStore::class.java)
    }
}