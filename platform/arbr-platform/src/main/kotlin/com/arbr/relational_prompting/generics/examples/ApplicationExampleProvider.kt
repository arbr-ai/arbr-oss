package com.arbr.relational_prompting.generics.examples

import com.arbr.relational_prompting.layers.object_translation.TemplateComponentSchema
import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.relational_prompting.services.embedding.model.TemplateElementLiteral
import com.arbr.relational_prompting.services.embedding.model.VectorResourceKeyValuePair
import com.arbr.db.public.tables.pojos.EmbeddedResourcePair
import reactor.core.publisher.Flux

interface ApplicationExampleProvider {
    /**
     * Publish a list of examples and return the created pairs.
     */
    fun <InputModel: SourcedStruct, OutputModel : SourcedStruct> publishAll(
        pairs: List<Pair<TemplateElementLiteral<InputModel>, TemplateElementLiteral<OutputModel>>>,
    ): Flux<EmbeddedResourcePair>

    /**
     * Publish and example and return the created pairs.
     */
    fun <InputModel: SourcedStruct, OutputModel : SourcedStruct> publish(
        inputLiteral: TemplateElementLiteral<InputModel>,
        outputLiteral: TemplateElementLiteral<OutputModel>,
    ): Flux<EmbeddedResourcePair> = publishAll(listOf(inputLiteral to outputLiteral))

    /**
     * Retrieve nearest neighbors to the input.
     * NOTE: Should take schema compatibility into account.
     */
    fun <InputModel: SourcedStruct, OutputModel : SourcedStruct> retrieveNearestNeighbors(
        inputSchema: TemplateComponentSchema<InputModel>,
        outputSchema: TemplateComponentSchema<OutputModel>,
        input: InputModel,
        numNeighborsToRetrieve: Long,
    ): Flux<VectorResourceKeyValuePair<InputModel, OutputModel>>
}