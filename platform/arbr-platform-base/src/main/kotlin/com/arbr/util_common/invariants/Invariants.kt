package com.arbr.util_common.invariants

import com.fasterxml.jackson.annotation.JsonValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Invariants private constructor(
    private val enabled: Boolean,
    private val failureLevel: FailureLevel,
) {
    enum class FailureLevel(@JsonValue val configValue: String) {
        LOG("log"),
        THROW("throw");
    }

    private val requirement = InvariantRequirement { condition ->
        try {
            if (!condition) {
                throw Exception()
            }
        } catch (e: Exception) {
            val message = e.stackTraceToString().split("\n").take(6).joinToString("\n") { it }

            when (failureLevel) {
                FailureLevel.LOG -> logger.error("Failed invariant", InvariantViolatedException(message, e))
                FailureLevel.THROW -> throw InvariantViolatedException(message, e)
            }
        }
    }

    private fun check(predicate: (require: (Boolean) -> Unit) -> Unit) {
        predicate(requirement::require)
    }

    companion object {
        private var invariants: Invariants? = null

        private val logger: Logger by lazy {
            LoggerFactory.getLogger(Invariants::class.java)
        }

        private fun getInstance(): Invariants? {
            return synchronized(this) {
                val currentValue = invariants
                if (currentValue != null) {
                    currentValue
                } else {
                    val config = InvariantsConfigurer.getConfig()
                    val newValue = if (config.enabled) {
                        Invariants(true, config.failureLevel)
                    } else {
                        null
                    }

                    newValue.also { invariants = it }
                }
            }
        }

        fun check(predicate: (require: (Boolean) -> Unit) -> Unit) {
            val invariantsInstance = getInstance() ?: return
            predicate(invariantsInstance.requirement::require)
        }

        /**
         * Override the configuration to enable or disable invariants.
         */
        fun setEnabled(enabled: Boolean, failureLevel: FailureLevel) {
            synchronized(this) {
                invariants = if (enabled) {
                    Invariants(true, failureLevel)
                } else {
                    null
                }
            }
        }
    }
}
