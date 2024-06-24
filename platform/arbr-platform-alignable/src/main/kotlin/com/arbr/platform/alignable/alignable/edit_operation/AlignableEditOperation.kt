package com.arbr.platform.alignable.alignable.edit_operation

import com.arbr.platform.alignable.alignable.MetricAlignable

/**
 * An AlignableEditOperation is an Edit, i.e. a member of the group that maps states to states, and it is alignable.
 * A Partial Order on AlignableEditOperations is the type of each object for the top level alignment process.
 */
interface AlignableEditOperation<Q, A: MetricAlignable<A, O2>, O2>: MetricAlignable<A, O2>, EditOperation<Q>