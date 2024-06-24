package com.arbr.aws.s3

class S3ServicesImpl(
    private val s3ServiceList: List<S3Service>,
): S3Services {
    private val s3ServiceMap: Map<AwsS3BucketLocalIdentifier, S3Service> by lazy {
        s3ServiceList
            .groupBy { it.bucketLocalIdentifier }
            .mapValues { (localIdentifier, services) ->
                check(services.isNotEmpty())
                if (services.size > 1) {
                    ensureNoInvalidDuplicates(localIdentifier, services)
                }
                services.first()
            }
    }

    private fun ensureNoInvalidDuplicates(
        localIdentifier: AwsS3BucketLocalIdentifier,
        services: List<S3Service>
    ) {
        // If configured buckets match on local identifier, they should resolve to the same bucket name
        val bucketNames = services.map { it.bucketName }
        check(bucketNames.distinct().size <= 1) {
            val joined = bucketNames.joinToString(", ")
            "Multiple resolved S3 bucket names corresponding to identifier $localIdentifier: $joined"
        }
    }

    override fun getService(bucketLocalIdentifier: AwsS3BucketLocalIdentifier): S3Service? {
        return s3ServiceMap[bucketLocalIdentifier]
    }
}