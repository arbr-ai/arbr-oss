package com.arbr.engine.services.kafka.consumer

import com.arbr.api.workflow.message.EndWorkflowTaskMessage
import com.arbr.api.workflow.message.SubmitWorkflowTaskMessage
import com.arbr.kafka.topic.base.ApiKafkaConsumerNode
import com.arbr.kafka.topic.base.ApiKafkaTopicNodeFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan("com.arbr.kafka.topic.base")
@ConditionalOnProperty(prefix = "arbr.kafka", name=["enabled"], havingValue="true", matchIfMissing = true)
class ApiKafkaConsumerNodeEngineConfig(
    private val apiKafkaTopicNodeFactory: ApiKafkaTopicNodeFactory,
) {

    @Bean
    fun workflowEventConsumerNode(): ApiKafkaConsumerNode<Unit, SubmitWorkflowTaskMessage>? {
        return apiKafkaTopicNodeFactory
            .consumerNode(SubmitWorkflowTaskMessage::class.java)
    }

    @Bean
    fun workflowEndEventConsumerNode(): ApiKafkaConsumerNode<Long, EndWorkflowTaskMessage>? {
        return apiKafkaTopicNodeFactory
            .consumerNode(EndWorkflowTaskMessage::class.java)
    }

}
