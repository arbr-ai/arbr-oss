package com.arbr.aws.s3

interface S3Services {

    fun getService(bucketLocalIdentifier: AwsS3BucketLocalIdentifier): S3Service?

}