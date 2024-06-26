/*
 * This file is generated by jOOQ.
 */
package com.arbr.db.`public`.routines


import com.arbr.db.binding.Vector1536
import com.arbr.db.binding.Vector1536Binding

import org.jooq.Field
import org.jooq.Parameter
import org.jooq.impl.AbstractRoutine
import org.jooq.impl.Internal
import org.jooq.impl.SQLDataType


/**
 * This class is generated by jOOQ.
 */
@Suppress("UNCHECKED_CAST")
open class VectorAccum : AbstractRoutine<Array<Double?>>("vector_accum", com.arbr.db.`public`.Public.PUBLIC, SQLDataType.FLOAT.array()) {
    companion object {

        /**
         * The parameter <code>public.vector_accum.RETURN_VALUE</code>.
         */
        val RETURN_VALUE: Parameter<Array<Double?>?> = Internal.createParameter("RETURN_VALUE", SQLDataType.FLOAT.array(), false, false)

        /**
         * The parameter <code>public.vector_accum._1</code>.
         */
        val _1: Parameter<Array<Double?>?> = Internal.createParameter("_1", SQLDataType.FLOAT.array(), false, true)

        /**
         * The parameter <code>public.vector_accum._2</code>.
         */
        val _2: Parameter<Vector1536?> = Internal.createParameter("_2", org.jooq.impl.DefaultDataType.getDefaultDataType("\"public\".\"vector\""), false, true, Vector1536Binding())
    }

    init {
        returnParameter = RETURN_VALUE
        addInParameter(_1)
        addInParameter(_2)
    }

    /**
     * Set the <code>_1</code> parameter IN value to the routine
     */
    fun set__1(value: Array<Double?>?): Unit = setValue(_1, value)

    /**
     * Set the <code>_1</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    fun set__1(field: Field<Array<Double?>?>): VectorAccum {
        setField(_1, field)
        return this
    }

    /**
     * Set the <code>_2</code> parameter IN value to the routine
     */
    fun set__2(value: Vector1536?): Unit = setValue(_2, value)

    /**
     * Set the <code>_2</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    fun set__2(field: Field<Vector1536?>): VectorAccum {
        setField(_2, field)
        return this
    }
}
