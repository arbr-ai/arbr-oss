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
open class VectorAvg : AbstractRoutine<Vector1536>("vector_avg", com.arbr.db.`public`.Public.PUBLIC, org.jooq.impl.DefaultDataType.getDefaultDataType("\"public\".\"vector\""), Vector1536Binding()) {
    companion object {

        /**
         * The parameter <code>public.vector_avg.RETURN_VALUE</code>.
         */
        val RETURN_VALUE: Parameter<Vector1536?> = Internal.createParameter("RETURN_VALUE", org.jooq.impl.DefaultDataType.getDefaultDataType("\"public\".\"vector\""), false, false, Vector1536Binding())

        /**
         * The parameter <code>public.vector_avg._1</code>.
         */
        val _1: Parameter<Array<Double?>?> = Internal.createParameter("_1", SQLDataType.FLOAT.array(), false, true)
    }

    init {
        returnParameter = RETURN_VALUE
        addInParameter(_1)
    }

    /**
     * Set the <code>_1</code> parameter IN value to the routine
     */
    fun set__1(value: Array<Double?>?): Unit = setValue(_1, value)

    /**
     * Set the <code>_1</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    fun set__1(field: Field<Array<Double?>?>): VectorAvg {
        setField(_1, field)
        return this
    }
}
