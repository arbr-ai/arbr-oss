package com.arbr.db.binding

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.r2dbc.postgresql.codec.Vector
import org.jooq.*
import org.jooq.conf.ParamType
import org.jooq.impl.DSL
import java.sql.Types

class Vector1536Binding: Binding<Any, Vector1536> {
    private val mapper = jacksonObjectMapper()

    private inline fun <reified T> classProxy() = T::class.java

    override fun converter(): Converter<Any, Vector1536> {
        return object : Converter<Any, Vector1536> {
            override fun from(databaseObject: Any?): Vector1536? {
                return databaseObject?.let { dbObject ->
                    if (dbObject is Vector) {
                        Vector1536(dbObject.vector.map { it.toDouble() }.toTypedArray())
                    } else {
                        val array = mapper.readValue(dbObject as String, classProxy<Array<Double>>())
                        Vector1536(array)
                    }
                }
            }

            override fun to(userObject: Vector1536): Any {
                return userObject.data
            }

            override fun fromType(): Class<Any> {
                return Any::class.java
            }

            override fun toType(): Class<Vector1536> {
                return Vector1536::class.java
            }
        }
    }

    override fun get(ctx: BindingGetSQLInputContext<Vector1536>) {
        throw UnsupportedOperationException()
    }

    override fun get(ctx: BindingGetStatementContext<Vector1536>) {
        ctx.convert(converter()).value(
            ctx.statement().getObject(ctx.index())
        )
    }

    override fun get(ctx: BindingGetResultSetContext<Vector1536>) {
        ctx.convert(converter()).value(
            ctx.resultSet().getObject(ctx.index())
        )
    }

    override fun set(ctx: BindingSetSQLOutputContext<Vector1536>) {
        throw UnsupportedOperationException()
    }

    override fun set(ctx: BindingSetStatementContext<Vector1536>) {
        val any = ctx.convert(converter()).value()
        ctx.statement().setObject(ctx.index(), any)
    }

    override fun register(ctx: BindingRegisterContext<Vector1536>) {
        ctx.statement().registerOutParameter(ctx.index(), Types.VARCHAR);
    }

    override fun sql(ctx: BindingSQLContext<Vector1536>) {
        if (ctx.render().paramType() == ParamType.INLINED) {
            ctx.render().visit(DSL.inline(ctx.convert(converter()).value())).sql("::vector")
        } else {
            ctx.render().sql(ctx.variable()).sql("::vector")
        }
    }

}