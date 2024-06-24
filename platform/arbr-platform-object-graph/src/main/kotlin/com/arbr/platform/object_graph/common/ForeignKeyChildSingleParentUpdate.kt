package com.arbr.platform.object_graph.common

import com.arbr.object_model.core.types.naming.NamedForeignKey

data class ForeignKeyChildSingleParentUpdate<ForeignKey: NamedForeignKey, T>(
    val foreignKey: ForeignKey,
    val parentUuid: String,
    val isAccepted: Boolean,
    val child: T,
    val updateDoesSetSameParent: Boolean,
)