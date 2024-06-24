package com.arbr.kafka.topic.base

import com.arbr.kafka.topic.config.ApiVersionedKafkaTopic
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.kafka.receiver.ReceiverOptions
import reactor.kafka.sender.SenderOptions
import reactor.kafka.sender.SenderResult
import reactor.kotlin.core.publisher.toMono
import java.time.Duration

@Component
@ConditionalOnProperty(prefix = "arbr.kafka", name=["enabled"], havingValue="true", matchIfMissing = true)
class ApiKafkaTopicNodeFactoryImpl(
    private val mapper: ObjectMapper,
    private val sslBundles: ObjectProvider<SslBundles>,
    private val properties: KafkaProperties,
    @Autowired(required = false)
    private val extraProperties: ApiKafkaExtraProperties?,
): ApiKafkaTopicNodeFactory {

    private lateinit var topics: List<ApiVersionedKafkaTopic<*, *>>
    private lateinit var producerNodes: List<ApiKafkaProducerNode<*, *>>

    @PostConstruct
    fun init() {
        val topics = makeTopics()
        this.topics = topics
        val producerTemplate = reactiveKafkaProducerTemplate()
        producerNodes = makeProducerNodes(
            producerTemplate,
            topics,
        )
    }

    private fun reactiveKafkaProducerTemplate(): ReactiveKafkaProducerTemplate<String, String> {
        val props = properties.buildProducerProperties(
            sslBundles.ifAvailable
        )
        if (extraProperties != null) {
            props.putAll(extraProperties.extraProperties)
        }
        return ReactiveKafkaProducerTemplate<String, String>(SenderOptions.create(props))
    }

    private fun <T> initializeClass(clz: Class<out T>): T? {
        val constructor = clz.constructors.firstOrNull {
            it.parameterCount == 0
        } ?: kotlin.run {
            logger.error("No arity-zero constructor found for Kafka topic $clz")
            return null
        }

        @Suppress("UNCHECKED_CAST")
        return constructor.newInstance() as T
    }

    private fun <K, T> initializeKafkaTopic(clz: Class<ApiVersionedKafkaTopic<out K, out T>>): ApiVersionedKafkaTopic<out K, out T>? {
        return initializeClass(clz)
    }

    private fun makeTopics(): List<ApiVersionedKafkaTopic<out Any?, out Any?>> {
        return ApiVersionedKafkaTopic::class.java.permittedSubclasses.mapNotNull { topicClass ->
            @Suppress("UNCHECKED_CAST")
            initializeKafkaTopic(topicClass as Class<ApiVersionedKafkaTopic<*, *>>)
        }
    }

    private fun <K, T> makeProducerNode(
        template: ReactiveKafkaProducerTemplate<String, String>,
        topic: ApiVersionedKafkaTopic<K, T>
    ): ApiKafkaProducerNode<K, T> {
        return ApiKafkaProducerNodeImpl(template, topic, mapper)
    }

    private fun makeProducerNodes(
        producerTemplate: ReactiveKafkaProducerTemplate<String, String>,
        topics: List<ApiVersionedKafkaTopic<out Any?, out Any?>>
    ): List<ApiKafkaProducerNode<*, *>> {
        return topics.map { topic ->
            makeProducerNode(producerTemplate, topic)
        }
    }

    /**
     * Procure a consumer node for the contextual type if it exists, or else null.
     */
    override fun <K, T> consumerNode(messageObjectClass: Class<T>): ApiKafkaConsumerNode<K, T>? {
        return topics
            .filterIsInstance<ApiVersionedKafkaTopic<K, T>>() // This filter doesn't achieve what we want on its own due to type erasure
            .firstOrNull {
                it.messageObjectClass == messageObjectClass
            }
            ?.let { topic ->
                ApiKafkaConsumerNodeImpl(sslBundles, properties, extraProperties, topic, mapper)
            }
    }

    /**
     * Procure a produce node for the contextual type if it exists, or else null.
     */
    override fun <K, T> producerNode(messageObjectClass: Class<T>): ApiKafkaProducerNode<K, T>? {
        return producerNodes
            .filterIsInstance<ApiKafkaProducerNode<K, T>>() // This filter doesn't achieve what we want on its own due to type erasure
            .firstOrNull {
                it.topic.messageObjectClass == messageObjectClass
            }
    }

    private class ApiKafkaConsumerNodeImpl<K, T>(
        private val sslBundles: ObjectProvider<SslBundles>,
        private val properties: KafkaProperties,
        private val extraProperties: ApiKafkaExtraProperties?,
        override val topic: ApiVersionedKafkaTopic<K, T>,
        private val mapper: ObjectMapper,
    ) : ApiKafkaConsumerNode<K, T> {

        private fun makeTemplate(
            topicName: String,
            groupId: String?,
        ): ReactiveKafkaConsumerTemplate<String, String> {
            val props: MutableMap<String, Any> = properties
                .buildConsumerProperties(
                    sslBundles.ifAvailable
                )
            if (extraProperties != null) {
                props.putAll(extraProperties.extraProperties)
            }

            if (groupId != null) {
                props["group.id"] = groupId
            }

            val basicReceiverOptions: ReceiverOptions<String, String> =
                ReceiverOptions.create<String, String>(props)
                    .commitBatchSize(1)
                    .commitInterval(Duration.ofSeconds(1L))

            return ReactiveKafkaConsumerTemplate(basicReceiverOptions.subscription(listOf(topicName)))
        }

        override fun receive(
            topicKey: K,
            offset: Long?,
            groupId: String?,
        ): Flux<ApiKafkaConsumerEvent<K, T>> {
            val topicName = topic.topicName(topicKey)
            val template = makeTemplate(topicName, groupId)
            val topicPartition = TopicPartition(topicName, SEEK_PARTITION)
            val seekMono = if (offset != null) {
                template.seek(topicPartition, offset)
            } else {
                Mono.empty()
            }

            return seekMono
                .thenMany(
                    template.receiveAtMostOnce()
                )
                .concatMap { record ->
                    val key = record.key()
                    try {
                        val value: String = record.value()
                        val messageObject = mapper.readValue(value, ApiKafkaTopicSerializedInstance::class.java)
                        val objectKey = mapper.convertValue(messageObject.objectKey, topic.messageObjectKeyClass)
                        val objectValue = mapper.convertValue(messageObject.objectValue, topic.messageObjectClass)

                        val instance = ApiKafkaTopicInstance(
                            messageObject.key,
                            messageObject.topicName,
                            messageObject.topicVersionId,
                            messageObject.objectKind,
                            messageObject.objectClassFqn,
                            messageObject.objectSchemaVersion,
                            objectKey,
                            objectValue
                        )

                        ApiKafkaConsumerEvent(
                            key,
                            record.offset(),
                            instance,
                        ).toMono()
                    } catch (e: Exception) {
                        logger.warn("Failed to parse event with key $key - dropping")
                        Mono.empty()
                    }
                }
        }

        companion object {
            /**
             * Partition to use when an offset seek is required on the template.
             */
            private const val SEEK_PARTITION = 0
        }
    }

    private class ApiKafkaProducerNodeImpl<K, T>(
        private val template: ReactiveKafkaProducerTemplate<String, String>,
        override val topic: ApiVersionedKafkaTopic<K, T>,
        private val mapper: ObjectMapper,
    ) : ApiKafkaProducerNode<K, T> {
        private fun handleSenderResult(
            senderResult: SenderResult<Void>
        ) {
            // Do nothing...
        }

        override fun send(messageKeyObject: K, messageObject: T): Mono<Void> {
            val objectSpecificId = topic.objectSpecificId(messageObject)
            val topicName = topic.topicName(messageKeyObject)
            val key = "${topicName}:${topic.versionId}:$objectSpecificId"
            val wrappedObject = ApiKafkaTopicInstance(
                key = key,
                topicName = topicName,
                topicVersionId = topic.versionId,
                objectKind = topic.messageObjectClass.simpleName,
                objectClassFqn = topic.messageObjectClass.name,
                objectSchemaVersion = objectSpecificId,
                objectKey = messageKeyObject,
                objectValue = messageObject
            )

            return template.send(
                ProducerRecord(
                    topicName,
                    key,
                    mapper.writeValueAsString(wrappedObject),
                )
            )
                .doOnNext { senderResult ->
                    handleSenderResult(senderResult)
                }
                .then()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ApiKafkaTopicNodeFactory::class.java)
    }
}