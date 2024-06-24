package com.arbr.aws.s3

import org.springframework.core.env.MissingRequiredPropertiesException

sealed class AwsS3BucketLocalIdentifier : EnvironmentSourcedValue<String> {
    /**
     * A bucket specified by a constant name
     */
    data class AdHoc(val bucketName: String) : AwsS3BucketLocalIdentifier(),
        EnvironmentSourcedValue<String> by EnvironmentSourcedValue.constant(bucketName)

    sealed class Property private constructor(
        val propertyName: String,
        environmentSourcedValue: EnvironmentSourcedValue<String>,
    ) : AwsS3BucketLocalIdentifier(), EnvironmentSourcedValue<String> by environmentSourcedValue {

        /**
         * Require the property value, or else throw an exception
         */
        constructor(propertyName: String) : this(
            propertyName,
            EnvironmentSourcedValue.fromProperty(propertyName) {
                throw MissingRequiredPropertiesException().also { ex ->
                    ex.missingRequiredProperties.add(propertyName)
                }
            }
        )

        /**
         * Check the property value, or else use the default
         */
        constructor(propertyName: String, defaultValue: String) : this(
            propertyName,
            EnvironmentSourcedValue.fromProperty(propertyName, defaultValue)
        )
    }

    /**
     * Well-known configured buckets:
     */
    object ArbrDatasets : Property("arbr.s3.bucket-datasets", "arbr-datasets")
}