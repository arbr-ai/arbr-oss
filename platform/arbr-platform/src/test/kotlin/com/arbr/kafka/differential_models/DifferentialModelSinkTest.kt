package com.arbr.kafka.differential_models

import com.arbr.kafka.topic.base.ApiKafkaConsumerEvent
import com.arbr.kafka.topic.base.ApiKafkaExtraProperties
import com.arbr.kafka.topic.base.ApiKafkaTopicNodeFactory
import com.arbr.kafka.topic.base.ApiKafkaTopicNodeFactoryImpl
import com.arbr.kafka.topic.model.TestDiffEvent
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

// TODO: Extract to config
private const val KAFKA_BOOTSTRAP_TEST_SERVERS = "127.0.0.1:9092"

@Configuration
open class TestConfig {

    @Bean
    open fun mapper(): ObjectMapper = jacksonObjectMapper()

}

@SpringBootTest(
    classes = [
        TestConfig::class,
        ApiKafkaTopicNodeFactory::class,
    ],
    properties = [
        "spring.kafka.bootstrap-servers=$KAFKA_BOOTSTRAP_TEST_SERVERS",
        "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer",
        "spring.kafka.consumer.group-id=arbr-engine",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
        "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
    ]
)
class DifferentialModelSinkTest {

    @SpyBean
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var sslBundles: ObjectProvider<SslBundles>

    @SpyBean
    private lateinit var kafkaProperties: KafkaProperties

    private lateinit var apiKafkaTopicNodeFactory: ApiKafkaTopicNodeFactory

    private val initialModel = TestModel("tomato", emptyList())
    private val outerInitialState = DifferentialModelTrackedState(
        initialModel,
        initialModel.hashCode().toString(),
    )

    private val outerSplitter = DifferentialModelSplitter<TestModel, TestDiffEvent> { tm ->
        listOf(
            TestDiffEvent(
                eventId = UUID.randomUUID().toString(),
                nameDiff = tm.name
            )
        ) + tm.items.map {
            TestDiffEvent(
                eventId = UUID.randomUUID().toString(),
                newItems = listOf(it)
            )
        }
    }

    private val outerReducer = object : DifferentialModelReducer<TestModel, TestDiffEvent> {
        override fun reduce(
            trackedState: DifferentialModelTrackedState<TestModel>,
            diff: TestDiffEvent
        ): DifferentialModelTrackedState<TestModel> {
            val newHash = (trackedState.hash.hashCode() * 31).xor(diff.hashCode())
            val newModel = trackedState.model.copy(
                name = diff.nameDiff ?: trackedState.model.name,
                items = (trackedState.model.items - diff.removeItems.toSet()) + diff.newItems
            )
            return DifferentialModelTrackedState(newModel, newHash.toString())
        }

        override fun associate(
            diff: TestDiffEvent,
            otherDiffs: List<TestDiffEvent>
        ): TestDiffEvent {
            return otherDiffs.fold(diff) { d, d1 ->
                d.copy(
                    nameDiff = d1.nameDiff ?: d.nameDiff,
                    newItems = (d.newItems - d1.removeItems.toSet()) + d1.newItems,
                    removeItems = (d.removeItems - d1.newItems.toSet()) + d1.removeItems,
                )
            }
        }
    }

    private val binding = object : DifferentialModelBinding<Unit, TestModel, TestDiffEvent> {
        override val modelClass: Class<TestModel> = TestModel::class.java
        override val diffClass: Class<TestDiffEvent> = TestDiffEvent::class.java
        override val initialState: DifferentialModelTrackedState<TestModel> = outerInitialState
        override val splitter: DifferentialModelSplitter<TestModel, TestDiffEvent> = outerSplitter
        override val reducer: DifferentialModelReducer<TestModel, TestDiffEvent> = outerReducer
        override val sinkFactory: DifferentialModelSinkFactory<Unit, TestModel, TestDiffEvent> =
            DifferentialModelSinkFactory { nf, b ->
                object : DifferentialModelSink<Unit, TestModel, TestDiffEvent>(
                    nf,
                    b
                ) {

                    private val stateMap =
                        ConcurrentHashMap<String, Pair<Long, DifferentialModelTrackedState<TestModel>>>()
                    private var tail = outerInitialState

                    @Synchronized
                    private fun innerIngestEvent(offset: Long, diff: TestDiffEvent) {
                        println("Ingesting offset=$offset diff=$diff")
                        val nextState = outerReducer.reduce(tail, diff)
                        stateMap.putIfAbsent(nextState.hash, offset to nextState)
                        tail = nextState
                    }

                    override fun ingestEvent(event: ApiKafkaConsumerEvent<Unit, TestDiffEvent>): Mono<Void> {
                        return Mono.fromCallable {
                            innerIngestEvent(event.offset, event.messageObject.objectValue)
                        }.then()
                    }

                    @Synchronized
                    private fun innerGetLatestTrackedState(): DifferentialModelTrackedState<TestModel> {
                        return tail
                    }

                    override fun getLatestTrackedState(): Mono<DifferentialModelTrackedState<TestModel>> {
                        return Mono.fromCallable {
                            innerGetLatestTrackedState()
                        }
                    }

                    @Synchronized
                    private fun innerGetOffset(hash: String): Long? {
                        return stateMap[hash]?.first
                    }

                    override fun getOffset(hash: String): Mono<Long> {
                        return Mono.fromCallable {
                            Optional.ofNullable(innerGetOffset(hash))
                        }.mapNotNull { it.getOrNull() }
                    }

                    @Synchronized
                    private fun innerGetState(hash: String): DifferentialModelTrackedState<TestModel>? {
                        return stateMap[hash]?.second
                    }

                    override fun getState(hash: String): Mono<DifferentialModelTrackedState<TestModel>> {
                        return Mono.fromCallable {
                            Optional.ofNullable(innerGetState(hash))
                        }.mapNotNull { it.getOrNull() }
                    }
                }
            }

    }

    private lateinit var differentialModelSink: DifferentialModelSink<Unit, TestModel, TestDiffEvent>

    @BeforeEach
    fun init() {
        kafkaProperties = kafkaProperties.also {
            it.bootstrapServers.clear()
            it.bootstrapServers.add(KAFKA_BOOTSTRAP_TEST_SERVERS)
        }

        apiKafkaTopicNodeFactory = ApiKafkaTopicNodeFactoryImpl(
            mapper,
            sslBundles,
            kafkaProperties,
            ApiKafkaExtraProperties(emptyMap()),
        ).also { it.init() }

        differentialModelSink = binding.makeDifferentialModelSink(apiKafkaTopicNodeFactory)
    }

    @Test
    fun inits() {
        val producerNode = apiKafkaTopicNodeFactory.producerNode<Unit, TestDiffEvent>(TestDiffEvent::class.java)!!

        Flux.interval(Duration.ofMillis(100L))
//            .take(10)
            .flatMap {
                val messageObject = TestDiffEvent(
                    it.toString(),
                )
                println("Sending $messageObject")

                producerNode.send(
                    Unit,
                    messageObject
                )
            }
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe()

        println("Waiting")
        val diffApplications = differentialModelSink.getNewestUpdates(Unit, DifferentialModelUpdateChannel.DISPLAY)
            .doOnEach {
                println("GOT SIGNAL $it")
            }
            .take(4)
            .collectList()
            .timeout(Duration.ofSeconds(8L))
            .block()!!

        var outState = outerInitialState
        for (diffApplication in diffApplications) {
            println(outState)
            println(diffApplication.nextHash)
            println(diffApplication.diff)
            println()

            outState = outerReducer.reduce(outState, diffApplication.diff)
        }

        println(outState)

        Assertions.assertEquals("tomato", outState.model.name)
    }

    private data class TestModel(
        val name: String,
        val items: List<Int>,
    )
}
