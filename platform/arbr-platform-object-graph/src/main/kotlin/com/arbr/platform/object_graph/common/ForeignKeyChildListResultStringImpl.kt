package com.arbr.platform.object_graph.common

data class ForeignKeyChildListResultStringImpl<T: Any>(
    override val parentKey: String,
    override val isAccepted: Boolean,
    override val children: List<T>,
): ForeignKeyChildListResult<String, T>
