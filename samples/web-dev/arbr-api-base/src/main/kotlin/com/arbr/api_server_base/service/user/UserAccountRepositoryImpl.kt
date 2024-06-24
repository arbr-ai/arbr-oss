package com.arbr.api_server_base.service.user

import com.arbr.db.public.tables.pojos.UserAccount
import com.arbr.db.public.tables.records.UserAccountRecord
import com.arbr.db.public.tables.references.USER_ACCOUNT
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class UserAccountRepositoryImpl(
    private val dslContext: DSLContext,
) : UserAccountRepository {
    override fun findById(id: Long): Mono<UserAccount> {
        return Flux.from(
            dslContext.select(
                USER_ACCOUNT.ID,
                USER_ACCOUNT.CREATION_TIMESTAMP,
                USER_ACCOUNT.USERNAME,
                USER_ACCOUNT.EMAIL,
                USER_ACCOUNT.PASSWORD_KEY,
                USER_ACCOUNT.ROLES_BITMASK,
                USER_ACCOUNT.AVATAR_URL,
            ).from(
                USER_ACCOUNT
            ).where(
                USER_ACCOUNT.ID.eq(id)
            )
        ).map { record: Record ->
            record.into(UserAccount::class.java)
        }.next()
    }

    override fun findAll(): Flux<UserAccount> {
        return Flux.from(
            dslContext.select(
                USER_ACCOUNT.ID,
                USER_ACCOUNT.CREATION_TIMESTAMP,
                USER_ACCOUNT.USERNAME,
                USER_ACCOUNT.EMAIL,
                USER_ACCOUNT.PASSWORD_KEY,
                USER_ACCOUNT.ROLES_BITMASK,
                USER_ACCOUNT.AVATAR_URL,
            ).from(
                USER_ACCOUNT
            )
        ).map { record: Record ->
            record.into(UserAccount::class.java)
        }
    }

    override fun deleteById(id: Long): Mono<Void> {
        return Flux.from(
            dslContext
                .deleteFrom(USER_ACCOUNT)
                .where(
                    USER_ACCOUNT.ID.eq(id)
                )
        ).then()
    }

    override fun findByUsername(username: String): Mono<UserAccount> {
        return Flux.from(
            dslContext.select(
                USER_ACCOUNT.ID,
                USER_ACCOUNT.CREATION_TIMESTAMP,
                USER_ACCOUNT.USERNAME,
                USER_ACCOUNT.EMAIL,
                USER_ACCOUNT.PASSWORD_KEY,
                USER_ACCOUNT.ROLES_BITMASK,
                USER_ACCOUNT.AVATAR_URL,
            ).from(
                USER_ACCOUNT
            ).where(
                USER_ACCOUNT.USERNAME.eq(username)
            )
        ).map { record: Record ->
            record.into(UserAccount::class.java)
        }.next()
    }

    override fun findByEmail(email: String): Mono<UserAccount> {
        return Flux.from(
            dslContext.select(
                USER_ACCOUNT.ID,
                USER_ACCOUNT.CREATION_TIMESTAMP,
                USER_ACCOUNT.USERNAME,
                USER_ACCOUNT.EMAIL,
                USER_ACCOUNT.PASSWORD_KEY,
                USER_ACCOUNT.ROLES_BITMASK,
                USER_ACCOUNT.AVATAR_URL,
            ).from(
                USER_ACCOUNT
            ).where(
                USER_ACCOUNT.EMAIL.eq(email)
            )
        ).map { record: Record ->
            record.into(UserAccount::class.java)
        }.next()
    }

    override fun create(
        creationTimestamp: Long,
        username: String?,
        email: String?,
        passwordKey: String,
        rolesBitmask: Int,
    ): Mono<UserAccount> {
        return Mono.from(
            dslContext
                .insertInto(
                    USER_ACCOUNT,
                    USER_ACCOUNT.CREATION_TIMESTAMP,
                    USER_ACCOUNT.USERNAME,
                    USER_ACCOUNT.EMAIL,
                    USER_ACCOUNT.PASSWORD_KEY,
                    USER_ACCOUNT.ROLES_BITMASK,
                )
                .values(
                    DSL.value(creationTimestamp),
                    DSL.value(username),
                    DSL.value(email),
                    DSL.value(passwordKey),
                    DSL.value(rolesBitmask),
                )
                .returning(
                    USER_ACCOUNT.ID,
                    USER_ACCOUNT.CREATION_TIMESTAMP,
                    USER_ACCOUNT.USERNAME,
                    USER_ACCOUNT.EMAIL,
                    USER_ACCOUNT.PASSWORD_KEY,
                    USER_ACCOUNT.ROLES_BITMASK,
                    USER_ACCOUNT.AVATAR_URL,
                )
        ).map { record ->
            record.into(UserAccount::class.java)
        }
    }

    override fun update(
        id: Long,
        update: (UserAccountRecord) -> Unit,
    ): Mono<UserAccount> {
        val record = USER_ACCOUNT.newRecord()
        update(record)
        record.set(USER_ACCOUNT.ID, id)

        return Mono.from(
            dslContext
                .update(USER_ACCOUNT)
                .set(
                    record
                )
                .returning(
                    USER_ACCOUNT.ID,
                    USER_ACCOUNT.CREATION_TIMESTAMP,
                    USER_ACCOUNT.USERNAME,
                    USER_ACCOUNT.EMAIL,
                    USER_ACCOUNT.PASSWORD_KEY,
                    USER_ACCOUNT.ROLES_BITMASK,
                    USER_ACCOUNT.AVATAR_URL,
                )
        ).map {
            it.into(UserAccount::class.java)
        }
    }
}