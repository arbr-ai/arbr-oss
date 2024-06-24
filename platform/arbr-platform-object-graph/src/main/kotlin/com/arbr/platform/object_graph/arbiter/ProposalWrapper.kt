package com.arbr.platform.object_graph.arbiter

import com.arbr.platform.object_graph.common.ObjectModel
import com.arbr.og.object_model.common.model.PropertyIdentifier
import com.arbr.og.object_model.common.model.Proposal
import com.arbr.og.object_model.common.model.ProposedValue
import com.arbr.platform.object_graph.impl.ObjectModelResource
import com.arbr.platform.object_graph.core.WorkflowSingleResourceProcessor
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.Logger

internal class ProposalWrapper<S : Any>(
    private val parentSingleResourceProcessor: WorkflowSingleResourceProcessor<*, *, *>,
    private val proposedValueStreamIdentifier: PropertyIdentifier,
    private val proposal: Proposal<S>,
    private val logger: Logger,
) : Proposal<S> {
    private val mapper = jacksonObjectMapper()

    override val acceptedValue: S?
        get() = proposal.acceptedValue

    override val proposedValue: S?
        get() = proposal.proposedValue

    private fun logString(value: Any?): String {
        return when (value) {
            null -> {
                "null"
            }

            is ObjectModelResource<*, *, *> -> {
                "${value::class.java.simpleName}[${value.uuid}] ${mapper.writeValueAsString(value)}"
            }

            is List<*> -> {
                "[" + value.joinToString(", ") { logString(it) } + "]"
            }

            is ObjectModel.ObjectValue<*, *, *, *> -> {
                "${value.typeName}[${value.id}] ${value.value}"
            }

            else -> {
                value.toString()
            }
        }
    }

    override fun accept() {
        logger.info(
            "ACCEPT ${proposedValueStreamIdentifier.resourceKey.name}[${proposedValueStreamIdentifier.resourceUuid}].${proposedValueStreamIdentifier.propertyKey.name} = ${
                logString(
                    proposedValue
                )
            }"
        )
        proposal.accept()

        // Send a notification to the processor
        parentSingleResourceProcessor.handleResourcePropertyValueUpdate(
            ProposedValue(
                proposedValueStreamIdentifier,
                proposedValue,
            )
        )
    }

    override fun reject() {
        logger.info(
            "REJECT ${proposedValueStreamIdentifier.resourceKey.name}[${proposedValueStreamIdentifier.resourceUuid}].${proposedValueStreamIdentifier.propertyKey.name} != ${
                logString(
                    proposedValue
                )
            }"
        )
        proposal.reject()
    }
}