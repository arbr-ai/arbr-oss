package com.arbr.relational_prompting.services.embedding.model

import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.relational_prompting.generics.model.ChatMessage
import com.arbr.relational_prompting.layers.object_translation.TemplateComponentSchema

data class TemplateElementLiteral<T: SourcedStruct>(
    /**
     * The schema of the component.
     */
    val schema: TemplateComponentSchema<T>,

    /**
     * The example object.
     */
    val obj: T,

    /**
     * The chat messages associated with the object, potentially hand-edited.
     */
    val chatMessages: List<ChatMessage>
)
