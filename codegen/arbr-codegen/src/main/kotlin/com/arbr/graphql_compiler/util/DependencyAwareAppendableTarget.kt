package com.arbr.graphql_compiler.util

interface DependencyAwareAppendableTarget<Key : Any, T : Any> : DependencyAware<Key, T>, AppendableTarget