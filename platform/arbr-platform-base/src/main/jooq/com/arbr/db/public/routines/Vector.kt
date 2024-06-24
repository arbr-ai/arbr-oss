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
open class Vector : AbstractRoutine<Vector1536>("vector", com.arbr.db.`public`.Public.PUBLIC, org.jooq.impl.DefaultDataType.getDefaultDataType("\"public\".\"vector\""), Vector1536Binding()) {
    companion object {

        /**
         * The parameter <code>public.vector.RETURN_VALUE</code>.
         */
        val RETURN_VALUE: Parameter<Vector1536?> = Internal.createParameter("RETURN_VALUE", org.jooq.impl.DefaultDataType.getDefaultDataType("\"public\".\"vector\""), false, false, Vector1536Binding())

        /**
         * The parameter <code>public.vector._1</code>.
         */
        val _1: Parameter<Vector1536?> = Internal.createParameter("_1", org.jooq.impl.DefaultDataType.getDefaultDataType("\"public\".\"vector\""), false, true, Vector1536Binding())

        /**
         * The parameter <code>public.vector._2</code>.
         */
        val _2: Parameter<Int?> = Internal.createParameter("_2", SQLDataType.INTEGER, false, true)

        /**
         * The parameter <code>public.vector._3</code>.
         */
        val _3: Parameter<Boolean?> = Internal.createParameter("_3", SQLDataType.BOOLEAN, false, true)
    }

    init {
        returnParameter = RETURN_VALUE
        addInParameter(_1)
        addInParameter(_2)
        addInParameter(_3)
    }

    /**
     * Set the <code>_1</code> parameter IN value to the routine
     */
    fun set__1(value: Vector1536?): Unit = setValue(_1, value)

    /**
     * Set the <code>_1</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    fun set__1(field: Field<Vector1536?>): Vector {
        setField(_1, field)
        return this
    }

    /**
     * Set the <code>_2</code> parameter IN value to the routine
     */
    fun set__2(value: Int?): Unit = setValue(_2, value)

    /**
     * Set the <code>_2</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    fun set__2(field: Field<Int?>): Vector {
        setField(_2, field)
        return this
    }

    /**
     * Set the <code>_3</code> parameter IN value to the routine
     */
    fun set__3(value: Boolean?): Unit = setValue(_3, value)

    /**
     * Set the <code>_3</code> parameter to the function to be used with a
     * {@link org.jooq.Select} statement
     */
    fun set__3(field: Field<Boolean?>): Vector {
        setField(_3, field)
        return this
    }
}
