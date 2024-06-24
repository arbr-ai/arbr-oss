package com.arbr.api_server_base.service.auth

import com.arbr.api.user.core.UserJwt
import com.arbr.api.user.core.UserRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.AuthorityUtils
import org.springframework.stereotype.Component
import java.util.*
import javax.crypto.SecretKey

@Component
class JwtService(
    private val privateKeys: PrivateKeys,
    @Value("\${arbr.auth.jwt_validity_duration_ms:3600000}")
    private val jwtValidityDurationMs: Long,
) {

    private fun jwtSecretKey(kind: PrivateKeyKind): SecretKey {
        val privateKey = privateKeys.forKind(kind)
        return Keys.hmacShaKeyFor(privateKey.toByteArray())
    }

    private fun getClaims(
        jwt: UserJwt,
        kind: PrivateKeyKind,
    ): Claims? {
        return try {
            val secretKey = jwtSecretKey(kind)
            val jwtParser = Jwts.parser().verifyWith(secretKey).build()

            val claimsJws: Jws<Claims> = jwtParser.parse(jwt.value)
                .accept(Jws.CLAIMS)
            claimsJws.payload
        } catch (e: Exception) {
            null
        }
    }

    private fun createJwt(
        userId: Long,
        username: String?,
        roles: List<UserRole>,
        kind: PrivateKeyKind,
    ): UserJwt {
        val subjectValue = userId.toString()
        val authorities = roles
            .map { GrantedAuthority { it.roleValue } }

        val claims = Jwts.claims().run {
            subject(subjectValue)
            if (authorities.isNotEmpty()) {
                add(AUTHORITIES_KEY, authorities.joinToString(",") { it.authority })
            }
            if (username != null) {
                add(USERNAME_KEY, username)
            }
            build()
        }

        val now = Date()
        val validity = Date(now.time + jwtValidityDurationMs)
        val jwtTokenValue = Jwts.builder().run {
            claims(claims)
            issuedAt(now)
            expiration(validity)
            signWith(jwtSecretKey(kind), Jwts.SIG.HS256)

            compact()
        }

        return UserJwt(jwtTokenValue)
    }

    fun getUserCredentialsForJwt(
        jwt: UserJwt,
        kind: PrivateKeyKind,
    ): RequestAuthenticator.UserCredentials? {
        val claims = getClaims(jwt, kind)
            ?: return null

        val userId = claims.subject.toLong()
        val authorities = claims[AUTHORITIES_KEY]?.let { authoritiesClaim ->
            AuthorityUtils
                .commaSeparatedStringToAuthorityList(authoritiesClaim.toString())
        } ?: AuthorityUtils.NO_AUTHORITIES
        val username = claims[USERNAME_KEY]?.toString()

        return RequestAuthenticator.UserCredentials(
            userId,
            username,
            jwt.value,
            authorities.mapNotNull { UserRole.fromSpringAuthorityValue(it.authority) }
        )
    }

    fun resolveUserCredentials(jwt: UserJwt): RequestAuthenticator.UserCredentials? {
        return PrivateKeyKind.values().firstNotNullOfOrNull { kind ->
            getUserCredentialsForJwt(jwt, kind)
        }
    }

    fun jwtBuilder(): JwtBuilder {
        return JwtBuilder(this::createJwt)
    }

    companion object {
        private const val AUTHORITIES_KEY = "roles"
        private const val USERNAME_KEY = "username"
    }
}