package com.arbr.engine.app.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
@ConditionalOnProperty(prefix = "topdown.scheduling", name=["enabled"], havingValue="true", matchIfMissing = true)
class SchedulingConfiguration
