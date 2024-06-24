package com.arbr.api_server_base.service.user

import com.arbr.db.public.tables.pojos.UserEmail
import com.arbr.db.public.tables.references.USER_EMAIL
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class UserEmailRepository(
    private val dslContext: DSLContext
) {
    fun findByUserId(userId: Long): Flux<UserEmail> {
        return Flux.from(
            dslContext
                .selectFrom(USER_EMAIL)
                .where(USER_EMAIL.USER_ID.eq(DSL.value(userId)))
        ).map { record ->
            record.into(UserEmail::class.java)
        }
    }

    fun findByEmailAddress(emailAddress: String): Mono<UserEmail> {
        return Mono.from(
            dslContext
                .selectFrom(USER_EMAIL)
                .where(USER_EMAIL.EMAIL_ADDRESS.eq(DSL.value(emailAddress)))
        ).map { record ->
            record.into(UserEmail::class.java)
        }
    }

    fun addEmailToUser(
        userId: Long,
        creationTimestamp: Long,
        updatedTimestamp: Long,
        emailAddress: String,
        source: String,
        ghVerified: Boolean?,
        ghPrimary: Boolean?
    ): Mono<UserEmail> {
        return Mono.from(
            dslContext
                .insertInto(
                    USER_EMAIL,
                    USER_EMAIL.USER_ID,
                    USER_EMAIL.CREATION_TIMESTAMP,
                    USER_EMAIL.UPDATED_TIMESTAMP,
                    USER_EMAIL.EMAIL_ADDRESS,
                    USER_EMAIL.SOURCE,
                    USER_EMAIL.GH_VERIFIED,
                    USER_EMAIL.GH_PRIMARY
                )
                .values(
                    DSL.value(userId),
                    DSL.value(creationTimestamp),
                    DSL.value(updatedTimestamp),
                    DSL.value(emailAddress),
                    DSL.value(source),
                    DSL.value(ghVerified),
                    DSL.value(ghPrimary)
                )
                .onConflict(USER_EMAIL.USER_ID, USER_EMAIL.EMAIL_ADDRESS)
                .doUpdate()
                .set(USER_EMAIL.UPDATED_TIMESTAMP, DSL.value(updatedTimestamp))
                .returning()
        ).map { record ->
            record.into(UserEmail::class.java)
        }
    }

    fun updateEmailVerification(
        userId: Long,
        emailAddress: String,
        ghVerified: Boolean
    ): Mono<Void> {
        return Mono.from(
            dslContext
                .update(USER_EMAIL)
                .set(USER_EMAIL.GH_VERIFIED, DSL.value(ghVerified))
                .where(
                    USER_EMAIL.USER_ID.eq(DSL.value(userId))
                        .and(USER_EMAIL.EMAIL_ADDRESS.eq(DSL.value(emailAddress)))
                )
        ).then()
    }

    fun deleteUserEmail(userId: Long, emailAddress: String): Mono<Void> {
        return Mono.from(
            dslContext
                .deleteFrom(USER_EMAIL)
                .where(
                    USER_EMAIL.USER_ID.eq(DSL.value(userId))
                        .and(USER_EMAIL.EMAIL_ADDRESS.eq(DSL.value(emailAddress)))
                )
        ).then()
    }
}
