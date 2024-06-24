package com.arbr.api_server_base.service.user

import com.arbr.db.public.tables.pojos.UserProject
import com.arbr.db.public.tables.references.USER_PROJECT
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class UserProjectRepository(
    private val dslContext: DSLContext,
) {

    fun insert(
        userId: Long,
        creationTimestamp: Long,
        projectName: String,
    ): Mono<UserProject> {
        return Mono.from(
            dslContext
                .insertInto(
                    USER_PROJECT,
                    USER_PROJECT.USER_ID,
                    USER_PROJECT.CREATION_TIMESTAMP,
                    USER_PROJECT.FULL_NAME,
                )
                .values(
                    DSL.value(userId),
                    DSL.value(creationTimestamp),
                    DSL.value(projectName),
                )
                .returning(DSL.asterisk())
        )
            .map { record ->
                record.into(UserProject::class.java)
            }
    }

    fun get(
        projectId: Long
    ): Mono<UserProject> {
        return Mono.from(
            dslContext
                .select(
                    DSL.asterisk()
                )
                .from(USER_PROJECT)
                .where(
                    USER_PROJECT.ID.eq(DSL.value(projectId))
                )
        )
            .map { record ->
                record.into(UserProject::class.java)
            }
    }

    fun getByName(
        userId: Long,
        projectName: String,
    ): Mono<UserProject> {
        return Mono.from(
            dslContext
                .select(
                    DSL.asterisk()
                )
                .from(USER_PROJECT)
                .where(
                    USER_PROJECT.USER_ID.eq(DSL.value(userId)),
                    USER_PROJECT.FULL_NAME.eq(DSL.value(projectName)),
                )
        )
            .map { record ->
                record.into(UserProject::class.java)
            }
    }

    fun getForUser(
        userId: Long,
    ): Flux<UserProject> {
        return Flux.from(
            dslContext
                .select(
                    DSL.asterisk()
                )
                .from(USER_PROJECT)
                .where(
                    USER_PROJECT.USER_ID.eq(DSL.value(userId))
                )
        )
            .map { record ->
                record.into(UserProject::class.java)
            }
    }
}
