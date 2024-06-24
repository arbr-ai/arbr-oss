package com.arbr.relational_prompting.generics.application_cache

import com.arbr.relational_prompting.layers.object_translation.TemplateComponentSchema
import com.arbr.og.object_model.common.values.collections.SourcedStruct
import com.arbr.relational_prompting.services.ai_application.model.TypedApplicationCompletion
import reactor.core.publisher.Mono

class DefaultApplicationCompletionCache: ApplicationCompletionCache {

    override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> get(
        applicationId: String,
        inputSchema: TemplateComponentSchema<InputModel>,
        outputSchema: TemplateComponentSchema<OutputModel>,
        input: InputModel,
        cacheKey: String
    ): Mono<TypedApplicationCompletion<InputModel, OutputModel>> {
        return Mono.error(NotImplementedError("No application completion cache configured"))
    }

    override fun <InputModel : SourcedStruct, OutputModel : SourcedStruct> set(
        applicationId: String,
        inputSchema: TemplateComponentSchema<InputModel>,
        outputSchema: TemplateComponentSchema<OutputModel>,
        typedApplicationCompletion: TypedApplicationCompletion<InputModel, OutputModel>
    ): Mono<Void> {
        return Mono.error(NotImplementedError("No application completion cache configured"))
    }
}
