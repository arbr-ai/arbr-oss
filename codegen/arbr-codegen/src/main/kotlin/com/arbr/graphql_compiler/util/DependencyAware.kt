package com.arbr.graphql_compiler.util

interface DependencyAware<Key : Any, T : Any> : Comparable<DependencyAware<Key, T>> {

    val key: Key

    val value: T

    fun getDependencyKeys(): List<Key>

}