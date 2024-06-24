package com.arbr.api_server_base.service.user

import com.arbr.db.public.tables.pojos.UserAccountLinkGithub
import com.arbr.db.public.tables.references.USER_ACCOUNT
import com.arbr.db.public.tables.references.USER_ACCOUNT_LINK_GITHUB
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class GitHubUserAccountRepository(
    private val dslContext: DSLContext,
) {
    fun getUserAccountGitHubLinkByUserId(userId: Long): Mono<UserAccountLinkGithub> {
        return Mono.from(
            dslContext
                .select(
                    USER_ACCOUNT_LINK_GITHUB.CREATION_TIMESTAMP,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_ID,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_KEY,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_APP_NAME,
                    USER_ACCOUNT_LINK_GITHUB.USER_ID,
                )
                .from(USER_ACCOUNT_LINK_GITHUB)
                .where(USER_ACCOUNT_LINK_GITHUB.USER_ID.eq(DSL.value(userId)))
        ).map { record ->
            record.into(UserAccountLinkGithub::class.java)
        }
    }

    fun getUserAccountGitHubLinkByUsername(username: String): Mono<UserAccountLinkGithub> {
        return Mono.from(
            dslContext
                .select(
                    USER_ACCOUNT_LINK_GITHUB.CREATION_TIMESTAMP,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_ID,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_KEY,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_APP_NAME,
                    USER_ACCOUNT_LINK_GITHUB.USER_ID,
                )
                .from(USER_ACCOUNT_LINK_GITHUB)
                .join(USER_ACCOUNT)
                .on(USER_ACCOUNT.ID.eq(USER_ACCOUNT_LINK_GITHUB.USER_ID))
                .where(USER_ACCOUNT.USERNAME.eq(DSL.value(username)))
        ).map { record ->
            record.into(UserAccountLinkGithub::class.java)
        }
    }

    fun getUserAccountGitHubLinkByGitHubId(gitHubId: String): Mono<UserAccountLinkGithub> {
        return Mono.from(
            dslContext
                .select(
                    USER_ACCOUNT_LINK_GITHUB.CREATION_TIMESTAMP,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_ID,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_KEY,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_APP_NAME,
                    USER_ACCOUNT_LINK_GITHUB.USER_ID,
                )
                .from(USER_ACCOUNT_LINK_GITHUB)
                .where(USER_ACCOUNT_LINK_GITHUB.GITHUB_ID.eq(DSL.value(gitHubId)))
        ).map { record ->
            record.into(UserAccountLinkGithub::class.java)
        }
    }

    fun attachGitHubAccountToUserAccount(
        userId: Long,
        username: String,
        creationTimestamp: Long,
        gitHubId: String,
        gitHubKey: String,
        gitHubAppName: String,
    ): Mono<UserAccountLinkGithub> {
        return Mono.from(
            dslContext
                .insertInto(
                    USER_ACCOUNT_LINK_GITHUB,
                    USER_ACCOUNT_LINK_GITHUB.CREATION_TIMESTAMP,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_ID,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_KEY,
                    USER_ACCOUNT_LINK_GITHUB.USER_ID,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_APP_NAME,
                )
                .values(
                    DSL.value(creationTimestamp),
                    DSL.value(gitHubId),
                    DSL.value(gitHubKey),
                    DSL.value(userId),
                    DSL.value(gitHubAppName),
                )
                .returning(
                    USER_ACCOUNT_LINK_GITHUB.CREATION_TIMESTAMP,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_ID,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_KEY,
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_APP_NAME,
                    USER_ACCOUNT_LINK_GITHUB.USER_ID,
                )
        ).map { record ->
            record.into(UserAccountLinkGithub::class.java)
        }
    }

    fun create(
        creationTimestamp: Long,
        username: String,
        passwordKey: String,
        gitHubId: String,
        gitHubKey: String,
        rolesBitmask: Int,
        avatarUrl: String?,
        gitHubAppName: String,
    ): Mono<UserAccountLinkGithub> {
        return Mono.from(
            dslContext
                .insertInto(
                    USER_ACCOUNT,
                    USER_ACCOUNT.CREATION_TIMESTAMP,
                    USER_ACCOUNT.USERNAME,
                    USER_ACCOUNT.PASSWORD_KEY,
                    USER_ACCOUNT.ROLES_BITMASK,
                    USER_ACCOUNT.AVATAR_URL,
                )
                .values(
                    DSL.value(creationTimestamp),
                    DSL.value(username),
                    DSL.value(passwordKey),
                    DSL.value(rolesBitmask),
                    DSL.value(avatarUrl),
                )
                .onConflict(
                    USER_ACCOUNT.USERNAME
                )
                .doUpdate()
                .set(USER_ACCOUNT.PASSWORD_KEY, DSL.value(passwordKey))
                .set(USER_ACCOUNT.ROLES_BITMASK, DSL.value(rolesBitmask))
                .set(USER_ACCOUNT.AVATAR_URL, DSL.value(avatarUrl))
                .returning(DSL.asterisk())
        ).flatMap {
            Mono.from(
                dslContext
                    .insertInto(
                        USER_ACCOUNT_LINK_GITHUB,
                        USER_ACCOUNT_LINK_GITHUB.CREATION_TIMESTAMP,
                        USER_ACCOUNT_LINK_GITHUB.GITHUB_ID,
                        USER_ACCOUNT_LINK_GITHUB.GITHUB_KEY,
                        USER_ACCOUNT_LINK_GITHUB.USER_ID,
                        USER_ACCOUNT_LINK_GITHUB.GITHUB_APP_NAME,
                    )
                    .values(
                        DSL.value(creationTimestamp),
                        DSL.value(gitHubId),
                        DSL.value(gitHubKey),
                        DSL.value(it.id!!),
                        DSL.value(gitHubAppName),
                    )
                    .returning(
                        USER_ACCOUNT_LINK_GITHUB.CREATION_TIMESTAMP,
                        USER_ACCOUNT_LINK_GITHUB.GITHUB_ID,
                        USER_ACCOUNT_LINK_GITHUB.GITHUB_KEY,
                        USER_ACCOUNT_LINK_GITHUB.GITHUB_APP_NAME,
                        USER_ACCOUNT_LINK_GITHUB.USER_ID,
                    )
            ).map { record ->
                record.into(UserAccountLinkGithub::class.java)
            }
        }
    }

    fun update(
        userId: Long,
        gitHubKey: String,
    ): Mono<Void> {
        return Mono.from(
            dslContext
                .update(
                    USER_ACCOUNT_LINK_GITHUB,
                )
                .set(
                    USER_ACCOUNT_LINK_GITHUB.GITHUB_KEY, DSL.value(gitHubKey)
                )
                .where(
                    USER_ACCOUNT_LINK_GITHUB.USER_ID.eq(DSL.value(userId))
                )
        ).then()
    }
}
