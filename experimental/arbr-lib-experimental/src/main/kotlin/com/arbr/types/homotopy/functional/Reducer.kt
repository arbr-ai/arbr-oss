package com.arbr.types.homotopy.functional

open class Reducer<Tr, V>(
    private val producer: Producer<V>,
    val preOrderVisitor: Ingestor<Tr> = Ingestor { },
    val postOrderVisitor: Ingestor<Tr> = Ingestor { },
) : Producer<V> by producer
