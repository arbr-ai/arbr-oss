package com.arbr.platform.alignable.alignable.graph

data class ConcreteHomogenousRootedDAG<T>(
    override val nodeValue: T,
    override val children: Map<String, Map<String, HomogenousRootedDAG<T>>>,
): HomogenousRootedDAG<T>

data class ConcreteHomogenousRootedDAG2<T>(
    override val nodeValue: T,
    override val children: Map<String, Map<String, ConcreteHomogenousRootedDAG2<T>>>,
): HomogenousRootedDAG<T>
