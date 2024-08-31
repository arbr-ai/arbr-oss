package com.arbr.aws.s3

fun interface EnvironmentSourcedValue<T> {

    fun getValueFromEnvironment(env: EnvironmentProperties): T

    companion object {
        inline fun <reified T> fromProperty(
            propertyName: String,
            crossinline onMissing: () -> Nothing
        ): EnvironmentSourcedValue<T> {
            return EnvironmentSourcedValue { env ->
                env.getProperty(propertyName, T::class.java) ?: onMissing()
            }
        }

        inline fun <reified T> fromProperty(
            propertyName: String,
            defaultValue: T,
        ): EnvironmentSourcedValue<T> {
            return EnvironmentSourcedValue { env ->
                env.getProperty(propertyName, T::class.java) ?: defaultValue
            }
        }

        fun <T : Any> constant(value: T): EnvironmentSourcedValue<T> {
            return EnvironmentSourcedValue { value }
        }
    }
}