package com.arbr.types.homotopy.functional

fun interface Producer<V> {
    fun yield(): V
}