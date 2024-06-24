package com.arbr.kafka.topic.config

import com.arbr.api.workflow.message.SubmitWorkflowTaskMessage
import com.arbr.kafka.topic.base.ApiKafkaTopicInstance
import com.arbr.util_common.typing.cls

class SubmitWorkflowKafkaTopic: ApiVersionedKafkaTopic<Unit, SubmitWorkflowTaskMessage> {
    override val messageObjectKeyClass: Class<Unit> = Unit::class.java
    override val messageObjectClass: Class<SubmitWorkflowTaskMessage> = SubmitWorkflowTaskMessage::class.java
    override val topicInstanceClass: Class<ApiKafkaTopicInstance<Unit, SubmitWorkflowTaskMessage>> = cls()

    override val versionId: String = "v0"

    override fun objectSpecificId(modelObject: SubmitWorkflowTaskMessage): String {
        return listOf(
            modelObject.userId.toString(),
            modelObject.projectFullName,
            modelObject.workflowType.serializedName,
            modelObject.requestCreationTimestampMs.toString(),
        ).joinToString(":") {
            it
                .replace("/", "_")
                .replace(":", "_")
        }
    }

    override fun topicName(topicKey: Unit): String {
        return TOPIC_NAME
    }

    companion object {
        const val TOPIC_NAME: String = "com.arbr.engine.workflow.submit"
    }
}
