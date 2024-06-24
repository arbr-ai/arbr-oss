package com.arbr.types.homotopy.functional

fun interface Ingestor<V> {
    fun ingest(v: V)
}