package com.arbr.util_common.invariants

fun interface InvariantRequirement {

    fun require(condition: Boolean)

}