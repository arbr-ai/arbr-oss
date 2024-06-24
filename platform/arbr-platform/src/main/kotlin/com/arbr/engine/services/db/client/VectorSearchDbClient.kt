package com.arbr.engine.services.db.client

import com.arbr.db.binding.Vector1536
import com.arbr.db.public.routines.references.cosineDistance
import com.arbr.db.public.tables.references.VECTOR_EMBEDDING
import org.jooq.*
import org.jooq.impl.DSL
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.kotlin.core.util.function.component1
import reactor.kotlin.core.util.function.component2
import reactor.kotlin.core.util.function.component3
import reactor.util.function.Tuple3
import reactor.util.function.Tuples

@Component
class VectorSearchDbClient(
    val dslContext: DSLContext,
) {

    final inline fun <reified R: Record> search(table: Table<R>): Builder<R> {
        return search(table, R::class.java)
    }

    fun <R: Record> search(table: Table<R>, recordClass: Class<R>): Builder<R> {
        return Builder(dslContext, table, recordClass)
    }

    class Builder<R: Record>(
        private val dslContext: DSLContext,
        private val table: Table<R>,
        private val recordClass: Class<R>,
    ) {
        private val dimensions = mutableListOf<Tuple3<Field<String?>, List<Double>, Double>>()
        private var condition: Condition = DSL.trueCondition()

        fun on(field: Field<String?>, valueVector: List<Double>, weight: Double = 1.0): Builder<R> {
            dimensions.add(Tuples.of(field, valueVector, weight))
            return this
        }

        fun where(condition: Condition): Builder<R> {
            this.condition = condition
            return this
        }

        fun nearest(k: Int): Flux<R> {
            val distanceField = dimensions.map { (_, vector, weight) ->
                cosineDistance(VECTOR_EMBEDDING.EMBEDDING, DSL.value(Vector1536(vector.toTypedArray())))
                    .times(DSL.value(weight))
            }.reduce { a, b ->
                a.plus(b)
            }.`as`("distance")

            val joinCondition = dimensions.map { (field, _, _) ->
                field.eq(VECTOR_EMBEDDING.VECTOR_ID)
            }.reduce { a, b ->
                a.or(b)
            }

            return Flux.from(
                dslContext
                    .select(
                        table.asterisk(),
                        distanceField,
                    )
                    .from(table)
                    .join(VECTOR_EMBEDDING)
                    .on(joinCondition)
                    .where(condition)
                    .orderBy(DSL.field("distance"))
                    .limit(k)
            ).map { record: Record ->
                record.into(recordClass)
            }
        }
    }

}