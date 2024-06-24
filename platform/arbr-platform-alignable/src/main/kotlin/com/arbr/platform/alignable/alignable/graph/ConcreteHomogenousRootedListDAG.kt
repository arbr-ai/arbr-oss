package com.arbr.platform.alignable.alignable.graph

data class ConcreteHomogenousRootedListDAG<T>(
    override val nodeValue: T,
    override val children: Map<String, List<HomogenousRootedListDAG<T>>>,
): HomogenousRootedListDAG<T>
