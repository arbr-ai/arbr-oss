package com.arbr.aws.s3

import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient

class S3ServiceFactoryImpl(
    private val awsCredentials: AwsCredentials
): S3ServiceFactory {

    /**
     * Fake a provider with the already-resolved credentials
     */
    private val constantCredentialsProvider = AwsCredentialsProvider { awsCredentials }

    private val s3AsyncClient: S3AsyncClient by lazy {
        try {
            S3AsyncClient.builder()
                .credentialsProvider(constantCredentialsProvider)
                .region(Region.US_EAST_1) // Specify the appropriate region
                .build()
        } catch (e: SdkClientException) {
            logger.error("Failed to load S3 client: ${e.message}")

            // Re-throw
            throw e
        }
    }

    override fun makeS3Service(bucketConfig: AwsS3BucketConfig): S3Service {
        return S3ServiceImpl(s3AsyncClient, bucketConfig.localIdentifier, bucketConfig.bucketName)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(S3ServiceFactoryImpl::class.java)
    }
}