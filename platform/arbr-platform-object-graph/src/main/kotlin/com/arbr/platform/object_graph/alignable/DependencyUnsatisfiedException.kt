package com.arbr.platform.object_graph.alignable

class DependencyUnsatisfiedException(operationStrings: List<String>): Exception("Unsatisfiable operations:\n${operationStrings.joinToString("\n") { it }}")