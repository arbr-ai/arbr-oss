package com.arbr.api_server_base.service.auth

import jakarta.annotation.PostConstruct
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.stereotype.Component

@Component
class AuthTokenAuthenticationFilter(
    userDetailsService: ReactiveUserDetailsService,
    private val requestAuthenticator: RequestAuthenticator,
): AuthenticationWebFilter(UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService)) {

    @PostConstruct
    fun init() {
        setServerAuthenticationConverter {
            requestAuthenticator.convertAuthentication(it).map<Authentication> { userCredentials ->
                UsernamePasswordAuthenticationToken(
                    userCredentials.username,
                    userCredentials.passwordKey,
                    userCredentials.roles.map { GrantedAuthority { it.roleValue } },
                )
            }
                        }
    }
}
