package com.arbr.relational_prompting.invariants

import com.arbr.util_common.invariants.InvariantViolatedException
import com.arbr.util_common.invariants.Invariants
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InvariantsTest {

    @BeforeEach
    fun init() {
        Invariants.setEnabled(true, Invariants.FailureLevel.THROW)
    }

    @Test
    fun throws() {
        Invariants.check { require ->
            require(true)
        }

        val err = org.junit.jupiter.api.assertThrows<InvariantViolatedException> {
            Invariants.check { require ->
                require(false)
            }
        }
        println(err.message)
    }

    @Test
    fun logs() {
        Invariants.setEnabled(true, Invariants.FailureLevel.LOG)

        Invariants.check { require ->
            require(true)
        }

        Invariants.check { require ->
            require(false)
        }
    }

}
