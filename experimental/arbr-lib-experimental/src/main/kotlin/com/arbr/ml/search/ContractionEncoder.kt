package com.arbr.ml.search

interface ContractionEncoder<E: Any> {

    fun identityCode(): Int

    fun encode(element: E): Int

    fun combine(leftCode: Int, rightCode: Int): Int

    fun decode(code: Int): E

}