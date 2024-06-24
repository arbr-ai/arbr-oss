package com.arbr.types.homotopy.functional

class VisitorImpl<V, W>(
    override val preOrderVisitor: Ingestor<V>,
    override val postOrderVisitor: Ingestor<W>,
): Visitor<V, W>