package com.arbr.engine.services.kafka.producer

import com.arbr.api.workflow.message.EndWorkflowTaskMessage
import com.arbr.api.workflow.message.SubmitWorkflowTaskMessage
import com.arbr.api.workflow.view_model.WorkflowPartialViewModel
import com.arbr.kafka.topic.base.ApiKafkaProducerNode
import com.arbr.kafka.topic.base.ApiKafkaTopicNodeFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan("com.arbr.kafka.topic.base")
@ConditionalOnProperty(prefix = "arbr.kafka", name=["enabled"], havingValue="true", matchIfMissing = true)
class ApiKafkaProducerNodeEngineConfig(
    private val apiKafkaTopicNodeFactory: ApiKafkaTopicNodeFactory,
) {

    @Bean
    fun workflowEventProducerNode(): ApiKafkaProducerNode<Long, WorkflowPartialViewModel>? {
        return apiKafkaTopicNodeFactory
            .producerNode(WorkflowPartialViewModel::class.java)
    }

    @Bean
    fun workflowSubmitProducerNode(): ApiKafkaProducerNode<Unit, SubmitWorkflowTaskMessage>? {
        return apiKafkaTopicNodeFactory
            .producerNode(SubmitWorkflowTaskMessage::class.java)
    }

    @Bean
    fun workflowEndProducerNode(): ApiKafkaProducerNode<Long, EndWorkflowTaskMessage>? {
        return apiKafkaTopicNodeFactory
            .producerNode(EndWorkflowTaskMessage::class.java)
    }

}
