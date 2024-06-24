package com.arbr.api_server_base.service.user

import com.arbr.db.public.tables.pojos.UserAccount
import com.arbr.db.public.tables.records.UserAccountRecord
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface UserAccountRepository {
    fun findAll(): Flux<UserAccount>

    fun findById(id: Long): Mono<UserAccount>

    fun findByUsername(username: String): Mono<UserAccount>

    fun findByEmail(email: String): Mono<UserAccount>

    fun create(
        creationTimestamp: Long,
        username: String?,
        email: String?,
        passwordKey: String,
        rolesBitmask: Int,
    ): Mono<UserAccount>

    fun update(
        id: Long,
        update: (UserAccountRecord) -> Unit,
    ): Mono<UserAccount>

    fun deleteById(id: Long): Mono<Void>
}
