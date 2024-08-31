package com.arbr.aws.s3

interface EnvironmentProperties {
    fun <T> getProperty(name: String, clazz: Class<T>): T?
}
