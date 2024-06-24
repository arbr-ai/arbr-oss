package com.arbr.relational_prompting.layers.object_translation

data class TemplateElements(
    val descriptionElements: TemplateDescriptionElements,
    val exampleValueElements: List<ExampleTemplateElements>,
    val inputValueElement: TemplateValueElement,
)