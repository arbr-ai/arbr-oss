package com.arbr.aws.s3

/**
 * A configured bucket with a local identifier and a loaded bucket name.
 */
data class AwsS3BucketConfig(
    val localIdentifier: AwsS3BucketLocalIdentifier,
    val bucketName: String,
)