package com.arbr.engine.services.db.config

data class PostgresConfigProperties(
    val host: String,
    val port: Int,
    val database: String,
    val user: String,
    val password: String,
    val useSsl: Boolean,
)