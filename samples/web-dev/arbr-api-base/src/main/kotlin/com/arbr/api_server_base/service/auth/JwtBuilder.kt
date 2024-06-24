package com.arbr.api_server_base.service.auth

import com.arbr.api.user.core.UserJwt
import com.arbr.api.user.core.UserRole

open class JwtBuilder(
    protected val createJwt: (
        userId: Long,
        username: String?,
        roles: List<UserRole>,
        kind: PrivateKeyKind,
    ) -> UserJwt
) {

    fun withKind(kind: PrivateKeyKind): WithKind {
        return WithKind(createJwt, kind)
    }

    open class WithKind(
        createJwt: (
            userId: Long,
            username: String?,
            roles: List<UserRole>,
            kind: PrivateKeyKind,
        ) -> UserJwt,
        protected val kind: PrivateKeyKind,
    ): JwtBuilder(createJwt) {

        fun withSubjectClaims(
            userId: Long,
            username: String?,
            roles: List<UserRole>,
        ): WithSubjectClaims {
            return WithSubjectClaims(createJwt, kind, userId, username, roles)
        }
    }

    class WithSubjectClaims(
        createJwt: (
            userId: Long,
            username: String?,
            roles: List<UserRole>,
            kind: PrivateKeyKind,
        ) -> UserJwt,
        kind: PrivateKeyKind,
        private val userId: Long,
        private val username: String?,
        private val roles: List<UserRole>,
    ): WithKind(createJwt, kind) {

        fun build(): UserJwt {
            return createJwt(userId, username, roles, kind)
        }
    }

}