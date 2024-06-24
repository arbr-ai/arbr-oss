package com.arbr.platform.autoconfigure.process

import com.arbr.engine.services.db.config.DefaultPostgresDSLContextFactory
import com.arbr.engine.services.db.config.PostgresConfigProperties
import com.arbr.engine.services.db.config.PostgresDSLContextFactory
import com.arbr.engine.services.user.*
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DbResourceStoreAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(PostgresConfigProperties::class)
    fun postgresConfigProperties(
        @Value("\${topdown.postgres.host}")
        host: String,
        @Value("\${topdown.postgres.port}")
        port: Int,
        @Value("\${topdown.postgres.database}")
        database: String,
        @Value("\${topdown.postgres.user}")
        user: String,
        @Value("\${topdown.postgres.password}")
        password: String,
        @Value("\${topdown.postgres.use_ssl}")
        useSsl: Boolean,
    ): PostgresConfigProperties {
        return PostgresConfigProperties(host, port, database, user, password, useSsl)
    }

    @Bean
    @ConditionalOnMissingBean(PostgresDSLContextFactory::class)
    fun postgresDSLContextFactory(): PostgresDSLContextFactory {
        return DefaultPostgresDSLContextFactory()
    }

    @Bean
    @ConditionalOnMissingBean(DSLContext::class)
    fun dslContext(
        configProperties: PostgresConfigProperties,
        postgresDSLContextFactory: PostgresDSLContextFactory,
    ): DSLContext {
        return postgresDSLContextFactory.makeDslContext(configProperties)
    }

    @Bean
    @ConditionalOnMissingBean(WorkflowResourceRepository::class)
    fun workflowResourceRepository(
        dslContext: DSLContext
    ): WorkflowResourceRepository {
        val factory = WorkflowResourceRepositoryFactory(
            dslContext
        )
        return factory.makeResourceRepository()
    }

    @Bean
    @ConditionalOnMissingBean(WorkflowStatusRepository::class)
    fun workflowStatusRepository(
        dslContext: DSLContext
    ): WorkflowStatusRepository {
        val factory = WorkflowStatusRepositoryFactory(
            dslContext
        )
        return factory.makeStatusRepository()
    }

    @Bean
    @ConditionalOnMissingBean(WorkflowWorkerRepository::class)
    fun workflowWorkerRepository(
        dslContext: DSLContext
    ): WorkflowWorkerRepository {
        val factory = WorkflowWorkerRepositoryFactory(
            dslContext
        )
        return factory.makeWorkflowWorkerRepository()
    }
}