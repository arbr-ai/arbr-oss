package com.arbr.util.invariants

fun interface InvariantRequirement {

    fun require(condition: Boolean)

}