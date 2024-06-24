package com.arbr.types.homotopy

import com.arbr.types.homotopy.htype.NodeHType

data class LocalPathQualifiedNode<Tr>(
    val localPathTokens: List<String>,
    val nodeHType: NodeHType<Tr, Tr>
)