package com.arbr.aws.s3

import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.nio.file.Path

interface S3Service {
    val bucketLocalIdentifier: AwsS3BucketLocalIdentifier
    val bucketName: String

    /**
     * Uploads a file to an S3 bucket.
     */
    fun uploadFile(key: String, filePath: Path): Mono<Void>

    /**
     * Uploads an object to an S3 bucket.
     */
    fun uploadObject(key: String, obj: Any): Mono<Void>

    /**
     * Get the text content for a single object.
     */
    fun getObjectText(
        key: String,
    ): Mono<String>

    /**
     * Get a single object.
     */
    fun <T : Any> getObject(
        key: String,
        objectClass: Class<T>,
    ): Mono<T>

    /**
     * List objects keys in the bucket starting with the prefix.
     */
    fun listObjectKeys(
        prefix: String,
    ): Flux<String>

    /**
     * List objects in the bucket starting with the prefix (and actually get them).
     */
    fun <T: Any> loadObjects(
        objectClass: Class<T>,
        prefix: String,
    ): Flux<T>
}
