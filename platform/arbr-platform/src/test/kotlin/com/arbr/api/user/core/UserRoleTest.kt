package com.arbr.api.user.core

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class UserRoleTest {

    @Test
    fun testBitmasks() {
        val roles = listOf(UserRole.USER)
        val bitmask = UserRole.toBitmask(roles)
        val parsedRoles = UserRole.fromBitmask(bitmask, false)

        Assertions.assertEquals(roles, parsedRoles)
    }

    @Test
    fun testBitmasksMulti() {
        val roles = listOf(UserRole.ADMIN, UserRole.USER)
        val bitmask = UserRole.toBitmask(roles)
        val parsedRoles = UserRole.fromBitmask(bitmask, false)

        Assertions.assertEquals(roles.toSet(), parsedRoles.toSet())
    }

}
