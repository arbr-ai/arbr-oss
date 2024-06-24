package com.arbr.kafka.topic.config

import com.arbr.api.workflow.message.EndWorkflowTaskMessage
import com.arbr.kafka.topic.base.ApiKafkaTopicInstance
import com.arbr.util_common.typing.cls


/**
 * Workflow ID -> Topic
 */
class EndWorkflowKafkaTopic : ApiVersionedKafkaTopic<Long, EndWorkflowTaskMessage> {
    override val messageObjectKeyClass: Class<Long> = Long::class.java
    override val messageObjectClass: Class<EndWorkflowTaskMessage> = EndWorkflowTaskMessage::class.java
    override val topicInstanceClass: Class<ApiKafkaTopicInstance<Long, EndWorkflowTaskMessage>> = cls()

    override val versionId: String = "v0"

    override fun objectSpecificId(modelObject: EndWorkflowTaskMessage): String {
        return listOf(
            modelObject.userId.toString(),
            modelObject.workflowId.toString(),
            modelObject.workflowStatus.serializedName,
        ).joinToString(":") {
            it
                .replace("/", "_")
                .replace(":", "_")
        }
    }

    override fun topicName(topicKey: Long): String {
        return "$TOPIC_NAME.$topicKey"
    }

    companion object {
        const val TOPIC_NAME: String = "com.arbr.engine.workflow.end"
    }
}
