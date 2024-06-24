package com.arbr.types.homotopy.functional

interface Visitor<V, W> {
    val preOrderVisitor: Ingestor<V>
    val postOrderVisitor: Ingestor<W>
}