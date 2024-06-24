package com.arbr.engine.services.db.client

import com.arbr.content_formats.jsonb.stripNulls
import com.fasterxml.jackson.databind.ObjectMapper
import com.arbr.db.public.tables.pojos.EmbeddedContent
import com.arbr.db.public.tables.records.EmbeddedContentRecord
import com.arbr.db.public.tables.references.EMBEDDED_CONTENT
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.asterisk
import org.jooq.impl.DSL.value
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private fun stripNulls(str: String): String = str.replace("\u0000", "")

@Component
class EmbeddedContentStore(
    private val dslContext: DSLContext,
    private val mapper: ObjectMapper,
) {
    fun insert(
        embeddedContentRecord: EmbeddedContentRecord,
    ): Mono<EmbeddedContent> {
        return Flux.from(
            dslContext
                .insertInto(
                    EMBEDDED_CONTENT,
                    EMBEDDED_CONTENT.CREATION_TIMESTAMP,
                    EMBEDDED_CONTENT.RESOURCE_ID,
                    EMBEDDED_CONTENT.VECTOR_ID,
                    EMBEDDED_CONTENT.SCHEMA_ID,
                    EMBEDDED_CONTENT.KIND,
                    EMBEDDED_CONTENT.EMBEDDING_CONTENT,
                    EMBEDDED_CONTENT.METADATA,
                )
                .values(
                    embeddedContentRecord.creationTimestamp,
                    embeddedContentRecord.resourceId,
                    embeddedContentRecord.vectorId,
                    embeddedContentRecord.schemaId,
                    embeddedContentRecord.kind,
                    stripNulls(embeddedContentRecord.embeddingContent),
                    embeddedContentRecord.metadata?.stripNulls(),
                )
                .returning(asterisk())
        ).map { record: Record ->
            record.into(EmbeddedContent::class.java)
        }
            .doOnError {
                logger.error("Failed to write embedded content: \n" + mapper.writeValueAsString(embeddedContentRecord))
            }
            .next()
    }

    fun update(
        embeddedContentRecord: EmbeddedContentRecord,
    ): Mono<EmbeddedContent> {
        return Flux.from(
            dslContext
                .insertInto(
                    EMBEDDED_CONTENT,
                    EMBEDDED_CONTENT.ID,
                    EMBEDDED_CONTENT.CREATION_TIMESTAMP,
                    EMBEDDED_CONTENT.RESOURCE_ID,
                    EMBEDDED_CONTENT.VECTOR_ID,
                    EMBEDDED_CONTENT.SCHEMA_ID,
                    EMBEDDED_CONTENT.KIND,
                    EMBEDDED_CONTENT.EMBEDDING_CONTENT,
                    EMBEDDED_CONTENT.METADATA,
                )
                .values(embeddedContentRecord)
                .onConflict()
                .doUpdate()
                .set(embeddedContentRecord)
                .returning(asterisk())
        ).map { record: Record ->
            record.into(EmbeddedContent::class.java)
        }.next()
    }

    fun get(
        id: Long,
    ): Mono<EmbeddedContent> {
        return Flux.from(
            dslContext
                .select(asterisk())
                .from(EMBEDDED_CONTENT)
                .where(EMBEDDED_CONTENT.ID.eq(value(id)))
        ).next().map { record: Record ->
            record.into(EmbeddedContent::class.java)
        }
    }

    fun getByInputResourceId(
        inputResourceId: Long,
    ): Flux<EmbeddedContent> {
        return Flux.from(
            dslContext
                .select(asterisk())
                .from(EMBEDDED_CONTENT)
                .where(EMBEDDED_CONTENT.RESOURCE_ID.eq(value(inputResourceId)))
        ).map { record: Record ->
            record.into(EmbeddedContent::class.java)
        }
    }

    fun delete(
        inputResourceId: Long,
    ): Flux<EmbeddedContent> {
        return Flux.from(
            dslContext
                .deleteFrom(EMBEDDED_CONTENT)
                .where(EMBEDDED_CONTENT.RESOURCE_ID.eq(value(inputResourceId)))
                .returning(asterisk())
        ).map { record: Record ->
            record.into(EmbeddedContent::class.java)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EmbeddedContentStore::class.java)
    }
}