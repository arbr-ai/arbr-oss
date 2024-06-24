package com.arbr.engine.services.db.config

import org.jooq.DSLContext

interface PostgresDSLContextFactory {
    fun makeDslContext(configProperties: PostgresConfigProperties): DSLContext
}