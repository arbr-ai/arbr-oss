package com.arbr.engine.services.db.client

import com.arbr.db.binding.Vector1536
import com.arbr.db.public.routines.references.cosineDistance
import com.arbr.db.public.tables.pojos.VectorEmbedding
import com.arbr.db.public.tables.references.VECTOR_EMBEDDING
import com.arbr.engine.services.db.model.VectorQueryResult
import com.arbr.engine.services.vector_db.model.Namespace
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

private fun stripNulls(str: String): String = str.replace("\u0000", "")

@Component
class VectorEmbeddingStore(
    private val dslContext: DSLContext,
) {
    fun get(
        vectorId: String,
        versionId: String,
    ): Flux<VectorEmbedding> {
        return Flux.from(
            dslContext
                .select(
                    VECTOR_EMBEDDING.VECTOR_ID,
                    VECTOR_EMBEDDING.NAMESPACE,
                    VECTOR_EMBEDDING.VERSION_ID,
                    VECTOR_EMBEDDING.SCHEMA_ID,
                    VECTOR_EMBEDDING.EMBEDDING_CONTENT,
                    VECTOR_EMBEDDING.EMBEDDING,
                )
                .from(VECTOR_EMBEDDING)
                .where(
                    VECTOR_EMBEDDING.VECTOR_ID.eq(DSL.value(vectorId)),
                    VECTOR_EMBEDDING.VERSION_ID.eq(DSL.value(versionId)),
                )
        ).map { record: Record ->
            record.into(VectorEmbedding::class.java)
        }
    }

    fun get(
        vectorIds: List<String>,
        versionId: String,
        namespace: Namespace,
    ): Flux<VectorEmbedding> {
        return Flux.from(
            dslContext
                .select(
                    VECTOR_EMBEDDING.VECTOR_ID,
                    VECTOR_EMBEDDING.NAMESPACE,
                    VECTOR_EMBEDDING.VERSION_ID,
                    VECTOR_EMBEDDING.SCHEMA_ID,
                    VECTOR_EMBEDDING.EMBEDDING_CONTENT,
                    VECTOR_EMBEDDING.EMBEDDING,
                )
                .from(VECTOR_EMBEDDING)
                .where(
                    VECTOR_EMBEDDING.VECTOR_ID.`in`(vectorIds.map { DSL.value(it) }),
                    VECTOR_EMBEDDING.VERSION_ID.eq(DSL.value(versionId)),
                    VECTOR_EMBEDDING.NAMESPACE.eq(DSL.value(namespace.namespaceName)),
                )
        ).map { record: Record ->
            record.into(VectorEmbedding::class.java)
        }
    }

    fun getMany(
        vectorIds: List<String>,
        versionId: String,
        namespace: Namespace,
        batchSize: Int,
    ): Flux<VectorEmbedding> {
        val windows = vectorIds.windowed(batchSize, step = batchSize, partialWindows = true)
        return Flux.fromIterable(windows)
            .concatMap { window ->
                get(
                    window,
                    versionId,
                    namespace,
                )
            }
    }

    fun getNearestNeighbors(
        embedding: Array<Double>,
        namespace: Namespace,
        schemaId: String?,
        topK: Long,
    ): Flux<VectorQueryResult> {
        return Flux.from(
            dslContext
                .select(
                    VECTOR_EMBEDDING.VECTOR_ID,
                    VECTOR_EMBEDDING.VERSION_ID,
                    VECTOR_EMBEDDING.NAMESPACE,
                    VECTOR_EMBEDDING.SCHEMA_ID,
                    VECTOR_EMBEDDING.EMBEDDING_CONTENT,
                    VECTOR_EMBEDDING.EMBEDDING,
                    cosineDistance(VECTOR_EMBEDDING.EMBEDDING, DSL.value(Vector1536(embedding)))
                        .`as`("distance")
                )
                .from(VECTOR_EMBEDDING)
                .where(VECTOR_EMBEDDING.NAMESPACE.eq(DSL.value(namespace.namespaceName)))
                .and(
                    if (schemaId == null) {
                        DSL.trueCondition()
                    } else {
                        VECTOR_EMBEDDING.SCHEMA_ID.eq(DSL.value(schemaId))
                    }
                )
                .orderBy(DSL.field("distance"))
                .limit(topK)
        ).map { record: Record ->
            record.into(VectorQueryResult::class.java)
        }
    }

    fun insert(
        vectorId: String,
        versionId: String,
        namespace: Namespace,
        schemaId: String,
        embeddingContent: String,
        embedding: Array<Double>,
    ): Mono<VectorEmbedding> {
        val strippedContent = stripNulls(embeddingContent)
        return Flux.from(
            dslContext
                .insertInto(
                    VECTOR_EMBEDDING,
                    VECTOR_EMBEDDING.VECTOR_ID,
                    VECTOR_EMBEDDING.VERSION_ID,
                    VECTOR_EMBEDDING.NAMESPACE,
                    VECTOR_EMBEDDING.SCHEMA_ID,
                    VECTOR_EMBEDDING.EMBEDDING_CONTENT,
                    VECTOR_EMBEDDING.EMBEDDING,
                )
                .values(
                    DSL.value(vectorId),
                    DSL.value(versionId),
                    DSL.value(namespace.namespaceName),
                    DSL.value(schemaId),
                    DSL.value(strippedContent),
                    DSL.value(Vector1536(embedding)),
                )
                .onConflict()
                .doUpdate()
                .set(VECTOR_EMBEDDING.VERSION_ID, DSL.value(versionId))
                .set(VECTOR_EMBEDDING.NAMESPACE, DSL.value(namespace.namespaceName))
                .set(VECTOR_EMBEDDING.SCHEMA_ID, DSL.value(schemaId))
                .set(VECTOR_EMBEDDING.EMBEDDING_CONTENT, DSL.value(strippedContent))
                .set(VECTOR_EMBEDDING.EMBEDDING, DSL.value(Vector1536(embedding)))
                .returning(DSL.asterisk())
        ).map { record: Record ->
            record.into(VectorEmbedding::class.java)
        }.next()
    }

    fun delete(vectorIds: List<String>): Mono<Void> {
        return Flux.from(
            dslContext
                .deleteFrom(VECTOR_EMBEDDING)
                .where(
                    VECTOR_EMBEDDING.VECTOR_ID.`in`(vectorIds.map { DSL.value(it) })
                )
        ).then()
    }
}
