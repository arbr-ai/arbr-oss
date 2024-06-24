package com.arbr.types.homotopy

import com.arbr.types.homotopy.keys.PropertyKey

data class PlainTypeNode(val ref: PlainType.Ref, val children: List<Pair<PropertyKey, PlainType>>)