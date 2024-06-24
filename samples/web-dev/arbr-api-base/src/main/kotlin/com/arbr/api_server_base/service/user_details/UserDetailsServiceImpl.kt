package com.arbr.api_server_base.service.user_details

import com.arbr.api.user.core.UserRole
import com.arbr.api_server_base.service.user.UserAccountRepository
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class UserDetailsServiceImpl(
    private val userAccountRepository: UserAccountRepository,
    private val passwordEncoder: PasswordEncoder,
) : ReactiveUserDetailsService {

    override fun findByUsername(username: String): Mono<UserDetails> {
        val dbUser = userAccountRepository.findByUsername(username)
            
        return dbUser.map { user ->
            val rolesBitmask = user.rolesBitmask
            val roles = UserRole.fromBitmask(rolesBitmask, false).map { it.roleValue }

            User.builder()
                .username(user.username)
                .password(passwordEncoder.encode(user.passwordKey))
                .roles(*roles.toTypedArray())
                .build()
        }
    }
}