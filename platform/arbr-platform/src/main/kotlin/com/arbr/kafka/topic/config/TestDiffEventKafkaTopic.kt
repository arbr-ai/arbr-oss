package com.arbr.kafka.topic.config

import com.arbr.kafka.topic.base.ApiKafkaTopicInstance
import com.arbr.kafka.topic.model.TestDiffEvent
import com.arbr.util_common.typing.cls

class TestDiffEventKafkaTopic: ApiVersionedKafkaTopic<Unit, TestDiffEvent> {
    override val messageObjectKeyClass: Class<Unit> = Unit::class.java
    override val messageObjectClass: Class<TestDiffEvent> = TestDiffEvent::class.java
    override val topicInstanceClass: Class<ApiKafkaTopicInstance<Unit, TestDiffEvent>> = cls()

    override val versionId: String = "v0"

    override fun objectSpecificId(modelObject: TestDiffEvent): String {
        return modelObject.eventId
    }

    override fun topicName(topicKey: Unit): String {
        return "com.arbr.engine.test.event"
    }
}
