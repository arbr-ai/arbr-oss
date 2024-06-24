package com.arbr.engine.services.db.client

import com.arbr.db.public.tables.pojos.IoPair
import com.arbr.db.public.tables.records.IoPairRecord
import com.arbr.db.public.tables.references.IO_PAIR
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL.asterisk
import org.jooq.impl.DSL.value
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class IoPairStore(
    private val dslContext: DSLContext,
) {
    fun insert(
        ioPairRecord: IoPairRecord,
    ): Mono<IoPair> {
        return Flux.from(
            dslContext
                .insertInto(
                    IO_PAIR,
                    IO_PAIR.CREATION_TIMESTAMP,
                    IO_PAIR.INPUT_RESOURCE_ID,
                    IO_PAIR.OUTPUT_RESOURCE_ID,
                )
                .values(ioPairRecord.intoList().drop(1))
                .returning(asterisk())
        ).map { record: Record ->
            record.into(IoPair::class.java)
        }.next()
    }

    fun get(
        inputResourceId: Long
    ): Flux<IoPair> {
        return Flux.from(
            dslContext
                .select(asterisk())
                .from(IO_PAIR)
                .where(IO_PAIR.INPUT_RESOURCE_ID.eq(value(inputResourceId)))
        )
            .map { record: Record ->
                record.into(IoPair::class.java)
            }
    }

    fun delete(
        inputResourceId: Long,
    ): Flux<IoPair> {
        return Flux.from(
            dslContext
                .deleteFrom(IO_PAIR)
                .where(IO_PAIR.INPUT_RESOURCE_ID.eq(value(inputResourceId)))
                .returning(asterisk())
        )
            .map { record: Record ->
                record.into(IoPair::class.java)
            }
    }
}