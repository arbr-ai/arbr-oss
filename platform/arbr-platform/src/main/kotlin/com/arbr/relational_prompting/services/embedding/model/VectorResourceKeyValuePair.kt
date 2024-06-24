package com.arbr.relational_prompting.services.embedding.model

import com.arbr.og.object_model.common.values.collections.SourcedStruct

data class VectorResourceKeyValuePair<K: SourcedStruct, V: SourcedStruct>(
    val vectorId: String,
    val key: TemplateElementLiteral<K>,
    val value: TemplateElementLiteral<V>,
)