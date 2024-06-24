package com.arbr.api_server_base.service.auth

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsProcessor
import org.springframework.web.cors.reactive.DefaultCorsProcessor
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity(useAuthorizationManager = true)
class SecurityConfig(
    private val authTokenAuthenticationFilter: AuthTokenAuthenticationFilter,
    @Autowired(required = false)
    private val corsProcessor: CorsProcessor?,
    private val corsConfiguration: CorsConfiguration,
) {
    private fun getCorsProcessor(): CorsProcessor = corsProcessor ?: DefaultCorsProcessor()

    @Bean
    fun securityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .addFilterAt(authTokenAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .csrf {
                it.disable()
            }
            .authorizeExchange {
                // TODO: Update
                it
                    .pathMatchers(
                        "/api/2/admin/**",
                    )
                    .hasRole("admin")
//                    .pathMatchers(
//                        "/api/1/github/oauth",
//                        "/api/1/github/user/registrations",
//                        "/api/1/users",
//                        "/api/1/users/login",
//                        "/api/1/users/logout",
//                        "/api/1/waitlist",
//                        "/api/1/stripe/**",
//                        "/api/1/product-configuration",
//                        "/",
//                        "/health",
//                    )
//                    .permitAll()
//                    .pathMatchers(
//                        "/api/1/github/user/*",
//                        "/api/1/github/user/repos/**",
//                        "/api/1/github/user/project/**",
//                        "/api/1/users/*",
//                        "/api/2/workflows/*",
//                    )
//                    .authenticated()
                    .anyExchange()
//                    .hasRole("admin")
                    .permitAll()
            }
            .httpBasic {
                it.disable()
            }
            .formLogin {
                val logoutEndpoint = "/api/1/users/logout"
                val entryPoint = RedirectServerAuthenticationEntryPointWithCors(
                    logoutEndpoint,
                    getCorsProcessor(),
                    corsConfiguration
                )
                val matcher = ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, logoutEndpoint)

                it
                    .authenticationEntryPoint(entryPoint)
                    .requiresAuthenticationMatcher(matcher)
                    .authenticationFailureHandler { webFilterExchange, _ ->
                        val exchange = webFilterExchange.exchange
                        getCorsProcessor().process(
                            corsConfiguration,
                            exchange,
                        )
                        exchange.response.statusCode = HttpStatus.UNAUTHORIZED
                        webFilterExchange.chain.filter(exchange)
                    }
            }
            .exceptionHandling { spec ->
                spec.accessDeniedHandler { exchange, _ ->
                    getCorsProcessor().process(
                        corsConfiguration,
                        exchange,
                    )
                    exchange.response.statusCode = HttpStatus.FORBIDDEN
                    Mono.empty()
                }
            }
            .build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SecurityConfig::class.java)
    }
}
