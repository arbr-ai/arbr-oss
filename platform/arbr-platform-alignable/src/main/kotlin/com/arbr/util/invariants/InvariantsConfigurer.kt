package com.arbr.util.invariants

object InvariantsConfigurer {

    private fun getConfigPropertyValue(
        propertyKey: String
    ): String? {
        val propertiesValue = System.getProperty(propertyKey)
        if (propertiesValue != null) {
            return propertiesValue
        }

        val envKey = propertyKey.replace(".", "_").uppercase()
        return System.getenv(envKey) ?: null
    }

    fun getConfig(): InvariantsConfig {
        val enabled = getConfigPropertyValue("topdown.invariants.enabled")?.toBooleanStrictOrNull() ?: false
        val failureLevelString = getConfigPropertyValue("topdown.invariants.failure_level") ?: "log"
        val failureLevel = Invariants.FailureLevel.entries
            .firstOrNull { it.configValue.equals(failureLevelString, ignoreCase = true) }
            ?: Invariants.FailureLevel.LOG
        return InvariantsConfig(enabled, failureLevel)
    }
}