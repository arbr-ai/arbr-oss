package com.arbr.engine.services.embedding.dal

import com.arbr.engine.services.db.client.VectorSearchDbClient
import com.arbr.relational_prompting.services.embedding.client.EmbeddingClient
import org.jooq.Condition
import org.jooq.Field
import org.jooq.Record
import org.jooq.Table
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import reactor.util.function.Tuple3
import reactor.util.function.Tuples

@Component
class VectorSearchDAL(
    val embeddingClient: EmbeddingClient,
    val vectorSearchDbClient: VectorSearchDbClient,
) {

    final inline fun <reified R : Record> search(table: Table<R>): Builder<R> {
        return Builder(embeddingClient, vectorSearchDbClient, table, R::class.java)
    }

    class Builder<R : Record>(
        private val embeddingClient: EmbeddingClient,
        private val vectorSearchDbClient: VectorSearchDbClient,
        private val table: Table<R>,
        private val recordClass: Class<R>,
    ) {
        private val dimensions = mutableListOf<Tuple3<Field<String?>, String, Double>>()
        private var condition: Condition = DSL.trueCondition()

        fun on(field: Field<String?>, value: String, weight: Double = 1.0): Builder<R> {
            dimensions.add(Tuples.of(field, value, weight))
            return this
        }

        fun where(condition: Condition): Builder<R> {
            this.condition = condition
            return this
        }

        fun nearest(k: Int): Flux<R> {
            val stringValues = dimensions.map { embeddingClient.preprocess(it.t2) }
            return embeddingClient.embed(stringValues).flatMapMany { vectors ->
                var searchBuilder = vectorSearchDbClient.search(table, recordClass)
                for ((i, vector) in vectors.withIndex()) {
                    if (vector == null) {
                        continue
                    }
                    val (field, _, weight) = dimensions[i]
                    searchBuilder = searchBuilder.on(field, vector, weight)
                }
                searchBuilder = searchBuilder.where(condition)

                searchBuilder.nearest(k)
            }
        }
    }
}