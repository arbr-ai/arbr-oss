package com.arbr.engine.services.db.config

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.jooq.impl.DefaultConfiguration
import org.jooq.tools.jdbc.JDBCUtils
import org.slf4j.LoggerFactory

class DefaultPostgresDSLContextFactory : PostgresDSLContextFactory {
    override fun makeDslContext(configProperties: PostgresConfigProperties): DSLContext {
        return configProperties.run {
            try {
                val baseUrl = "r2dbc:pool:postgresql:file://$host:$port/$database"
                val queryString = if (useSsl) "?ssl=true&sslMode=require" else ""
                val connectionFactory: ConnectionFactory = ConnectionFactories.get(
                    ConnectionFactoryOptions
                        .parse(baseUrl + queryString)
                        .mutate()
                        .option(ConnectionFactoryOptions.USER, user)
                        .option(ConnectionFactoryOptions.PASSWORD, password)
                        .build()
                )

                DSL
                    .using(
                        DefaultConfiguration()
                            .set(connectionFactory)
                            .set(JDBCUtils.dialect(connectionFactory))
                    )
            } catch (e: Exception) {
                logger.error("Error initializing Postgres config: ${e.message}")
                throw e
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(DefaultPostgresDSLContextFactory::class.java)
    }
}
