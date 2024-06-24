package com.arbr.aws.s3

fun interface S3ServiceFactory {

    fun makeS3Service(
        bucketConfig: AwsS3BucketConfig,
    ): S3Service
}