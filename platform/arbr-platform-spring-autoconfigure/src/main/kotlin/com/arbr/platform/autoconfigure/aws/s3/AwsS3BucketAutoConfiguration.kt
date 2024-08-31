package com.arbr.platform.autoconfigure.aws.s3

import com.arbr.aws.s3.AwsS3BucketConfig
import com.arbr.aws.s3.AwsS3BucketLocalIdentifier
import com.arbr.aws.s3.EnvironmentProperties
import com.arbr.util_common.reflection.ReflectionUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment

@Configuration
class AwsS3BucketAutoConfiguration(
    private val environment: Environment,
) {
    private val environmentProperties = object : EnvironmentProperties {
        override fun <T> getProperty(name: String, clazz: Class<T>): T? {
            return environment.getProperty(name, clazz)
        }
    }

    private fun getPropertySourcedBucketIdentifiers(): List<AwsS3BucketLocalIdentifier> {
        return ReflectionUtils.sealedMemberInstances(AwsS3BucketLocalIdentifier.Property::class)
    }

    private fun propertySourcedBucketConfigs(): List<AwsS3BucketConfig> {
        val identifiers = getPropertySourcedBucketIdentifiers()
        return identifiers.map { identifier ->
            val bucketName = identifier.getValueFromEnvironment(environmentProperties)
            AwsS3BucketConfig(identifier, bucketName)
        }
    }

    @Bean
    fun bucketConfigs(
        /**
         * Ad Hoc bucket identifiers configured elsewhere
         */
        adHocBucketLocalIdentifiers: List<AwsS3BucketLocalIdentifier.AdHoc>,
    ): List<AwsS3BucketConfig> {
        val propertyConfigs = propertySourcedBucketConfigs()
        val adHocConfigs = adHocBucketLocalIdentifiers.map { identifier ->
            val bucketName = identifier.getValueFromEnvironment(environmentProperties)
            AwsS3BucketConfig(identifier, bucketName)
        }
        return propertyConfigs + adHocConfigs
    }
}
