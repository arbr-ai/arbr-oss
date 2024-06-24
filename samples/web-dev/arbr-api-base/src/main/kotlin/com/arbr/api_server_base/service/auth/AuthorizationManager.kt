package com.arbr.api_server_base.service.auth

import com.arbr.api.user.core.UserRole
import com.arbr.api_server_base.service.user.UserService
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
@Qualifier("authorizationManager")
class AuthorizationManager(
    private val userService: UserService,
    @Value("\${topdown.auth.enable-features:false}")
    private val featuresEnabled: Boolean,
    @Value("\${topdown.auth.enable-subscriptions:false}")
    private val subscriptionFeaturesEnabled: Boolean,
    @Value("\${spring.profiles.active:}")
    private val profiles: String,
) {
    private val activeProfiles = profiles.split(",").map { it.trim().lowercase() }
    private val isLocal = "local" in activeProfiles

    @PostConstruct
    fun init() {
        logger.info("AuthManager profiles active: $activeProfiles")
    }

    class Builder(
        private val userService: UserService,
        private val localPrincipal: LocalPrincipal,
    ) {
        private var loggedInUserId: Long? = null
        private val roles = ConcurrentHashMap<UserRole, Unit>()

        fun loggedInAs(userId: Long): Builder {
            return this.also {
                loggedInUserId = userId
            }
        }

        fun isAdmin(): Builder {
            return this.also {
                roles[UserRole.ADMIN] = Unit
            }
        }

        fun isApi(): Builder {
            return this.also {
                roles[UserRole.API] = Unit
            }
        }

        fun isUser(): Builder {
            return this.also {
                roles[UserRole.USER] = Unit
            }
        }

        fun isAlphaTester(): Builder {
            return this.also {
                roles[UserRole.ALPHA_TESTER] = Unit
            }
        }

        fun build(): Mono<Boolean> {
            val userAuthorityValues = localPrincipal.authorities.map { it.authority }

            return if (roles.map { it.key.springAuthority() }.intersect(userAuthorityValues.toSet()).isNotEmpty()) {
                Mono.just(true)
            } else {
                val loggedInUserId = loggedInUserId
                if (loggedInUserId == null) {
                    Mono.just(false)
                } else {
                    userService
                        .getUserByUsername(localPrincipal.username)
                        .map {
                            it.id == loggedInUserId
                        }
                        .switchIfEmpty {
                            logger.warn("No user matching username ${localPrincipal.username}")

                            // Not logged in but logged-in state required
                            Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED))
                        }
                }
            }
        }
    }

    private fun authorizeLocalPrincipal(localPrincipal: LocalPrincipal): Builder {
        return Builder(userService, localPrincipal)
    }

    fun authorize(principal: Any?): Builder {
        val localPrincipal = when (principal) {
            is User -> LocalPrincipal.UserPrincipal(principal)
            is AbstractAuthenticationToken -> LocalPrincipal.AuthTokenPrincipal(principal)
            else -> LocalPrincipal.Anonymous
        }

        return Builder(userService, localPrincipal)
    }

    fun authorize(user: User): Builder {
        return Builder(userService, LocalPrincipal.UserPrincipal(user))
    }

    /**
     * Whether the user represented by the principal should have access to core features.
     */
    fun hasFeatureAccess(user: Any?): Mono<Boolean> {
        val localPrincipal = when (user) {
            is User -> LocalPrincipal.UserPrincipal(user)
            is AbstractAuthenticationToken -> LocalPrincipal.AuthTokenPrincipal(user)
            else -> LocalPrincipal.Anonymous
        }

        return if (featuresEnabled) {
            authorizeLocalPrincipal(localPrincipal).isUser().isAlphaTester().isAdmin().isApi().build()
        } else {
            authorizeLocalPrincipal(localPrincipal).isAlphaTester().isAdmin().isApi().build()
        }
    }

    /**
     * Whether the user represented by the principal should have access to the given subscription environment by name.
     */
    private fun hasSubscriptionEnvironmentAccess(localPrincipal: LocalPrincipal, environment: String): Mono<Boolean> {
        // Subscriptions have been removed
        return Mono.just(true)
    }

    fun hasSubscriptionEnvironmentAccess(user: Any?, environment: String): Mono<Boolean> {
        val localPrincipal = when (user) {
            is User -> LocalPrincipal.UserPrincipal(user)
            is AbstractAuthenticationToken -> LocalPrincipal.AuthTokenPrincipal(user)
            else -> LocalPrincipal.Anonymous
        }

        return hasSubscriptionEnvironmentAccess(localPrincipal, environment)
    }

    sealed class LocalPrincipal(
        val username: String,
        val authorities: Collection<GrantedAuthority>,
    ) {
        class UserPrincipal(
            user: User
        ) : LocalPrincipal(
            user.username,
            user.authorities
        )

        class AuthTokenPrincipal(
            authToken: AbstractAuthenticationToken
        ) : LocalPrincipal(
            authToken.name,
            authToken.authorities
        )

        data object Anonymous : LocalPrincipal(
            UUID.randomUUID().toString(),
            emptySet(),
        )
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AuthorizationManager::class.java)
    }
}

