package com.arbr.api.user.core

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonValue

enum class UserRole(@JsonValue val roleValue: String) {
    /**
     * Admin super-user permissions.
     */
    ADMIN("admin"),

    /**
     * API permissions.
     */
    API("api"),

    /**
     * Alpha test user
     */
    ALPHA_TESTER("alpha_tester"),

    /**
     * Basic user permissions.
     */
    USER("user");

    @JsonIgnore
    fun springAuthority(): String {
        return "ROLE_${roleValue}"
    }

    companion object {
        /**
         * Roles in bitmask from LSB leftwards
         */
        private val bitmaskRolesRevList: List<UserRole> = listOf(
            USER,
            API,
            ADMIN,
            ALPHA_TESTER,
        )

        fun fromBitmask(bitmask: Int, hasAlphaCode: Boolean): List<UserRole> {
            val roles = mutableListOf<UserRole>()
            var bm = bitmask
            for (role in bitmaskRolesRevList) {
                if (bm % 2 == 1) {
                    roles.add(role)
                }
                bm /= 2
            }

            if (hasAlphaCode) {
                roles.add(ALPHA_TESTER)
            }

            return roles
        }

        fun toBitmask(roles: List<UserRole>): Int {
            var bm = 0
            for (role in bitmaskRolesRevList.reversed()) {
                bm *= 2
                if (role in roles) {
                    bm += 1
                }
            }
            return bm
        }

        fun fromSpringAuthorityValue(authority: String): UserRole? {
            return UserRole.values().firstOrNull {
                it.springAuthority() == authority
            }
        }
    }
}
