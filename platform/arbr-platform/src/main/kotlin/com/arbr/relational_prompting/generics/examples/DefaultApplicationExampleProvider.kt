package com.arbr.relational_prompting.generics.examples

import com.arbr.relational_prompting.layers.object_translation.TemplateComponentSchema
import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.relational_prompting.services.embedding.model.TemplateElementLiteral
import com.arbr.relational_prompting.services.embedding.model.VectorResourceKeyValuePair
import com.arbr.db.public.tables.pojos.EmbeddedResourcePair
import reactor.core.publisher.Flux

class DefaultApplicationExampleProvider : ApplicationExampleProvider {
    /**
     * Publish a list of examples and return the created pairs.
     */
    override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> publishAll(
        pairs: List<Pair<TemplateElementLiteral<InputModel>, TemplateElementLiteral<OutputModel>>>
    ): Flux<EmbeddedResourcePair> {
        return Flux.error(NotImplementedError("No example provider configured"))
    }

    override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> retrieveNearestNeighbors(
        inputSchema: TemplateComponentSchema<InputModel>,
        outputSchema: TemplateComponentSchema<OutputModel>,
        input: InputModel,
        numNeighborsToRetrieve: Long
    ): Flux<VectorResourceKeyValuePair<InputModel, OutputModel>> =
        Flux.error(NotImplementedError("No example provider configured"))
}