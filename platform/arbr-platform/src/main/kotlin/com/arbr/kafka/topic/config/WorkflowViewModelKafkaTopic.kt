package com.arbr.kafka.topic.config

import com.arbr.api.workflow.view_model.WorkflowPartialViewModel
import com.arbr.kafka.topic.base.ApiKafkaTopicInstance
import com.arbr.util_common.typing.cls

/**
 * Topic mapping workflow ID -> partial view model stream
 */
class WorkflowViewModelKafkaTopic: ApiVersionedKafkaTopic<Long, WorkflowPartialViewModel> {
    override val messageObjectKeyClass: Class<Long> = Long::class.java
    override val messageObjectClass: Class<WorkflowPartialViewModel> = WorkflowPartialViewModel::class.java
    override val topicInstanceClass: Class<ApiKafkaTopicInstance<Long, WorkflowPartialViewModel>> = cls()

    override val versionId: String = "v0"

    override fun objectSpecificId(modelObject: WorkflowPartialViewModel): String {
        return modelObject.workflowId
    }

    override fun topicName(topicKey: Long): String {
        return "$TOPIC_NAME.$topicKey"
    }

    companion object {
        private const val TOPIC_NAME: String = "com.arbr.engine.workflow.view_model"
    }
}
