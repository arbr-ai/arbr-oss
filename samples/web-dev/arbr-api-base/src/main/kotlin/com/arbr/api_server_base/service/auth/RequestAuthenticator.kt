package com.arbr.api_server_base.service.auth

import com.arbr.api.user.core.UserJwt
import com.arbr.api.user.core.UserRole
import com.arbr.api_server_base.service.user.UserService
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class RequestAuthenticator(
    private val userService: UserService,
    private val jwtService: JwtService,
) {
    data class UserCredentials(
        val userId: Long,
        val username: String?,
        val passwordKey: String,
        val roles: List<UserRole>,
    )

    fun convertAuthentication(exchange: ServerWebExchange): Mono<UserCredentials> {
        val authHeaderValue = exchange.request.headers[HttpHeaders.AUTHORIZATION]
            ?.firstOrNull()
            ?: return Mono.empty()

        val jwtString = if (authHeaderValue.startsWith(BEARER_PREFIX)) {
            authHeaderValue.drop(BEARER_PREFIX.length)
        } else {
            return Mono.empty()
        }
        val jwt = UserJwt(jwtString)

        val userCredentials = jwtService.resolveUserCredentials(jwt)
            ?: return Mono.empty() // No match

        val userId = userCredentials.userId
        val roles = userCredentials.roles

        return userService.getUserPasswordKey(userId)
            .map { passwordKey ->
                UserCredentials(
                    userId,
                    userCredentials.username,
                    passwordKey,
                    roles,
                )
            }
    }

    companion object {
        private const val BEARER_PREFIX = "Bearer "
    }
}
