package com.arbr.aws.s3

import org.springframework.core.env.Environment
import org.springframework.core.env.getProperty

fun interface EnvironmentSourcedValue<T> {

    fun getValueFromEnvironment(env: Environment): T

    companion object {
        inline fun <reified T> fromProperty(
            propertyName: String,
            crossinline onMissing: () -> Nothing
        ): EnvironmentSourcedValue<T> {
            return EnvironmentSourcedValue { env ->
                env.getProperty<T>(propertyName) ?: onMissing()
            }
        }

        inline fun <reified T> fromProperty(
            propertyName: String,
            defaultValue: T,
        ): EnvironmentSourcedValue<T> {
            return EnvironmentSourcedValue { env ->
                env.getProperty<T>(propertyName) ?: defaultValue
            }
        }

        fun <T : Any> constant(value: T): EnvironmentSourcedValue<T> {
            return EnvironmentSourcedValue { value }
        }
    }
}