package com.arbr.aws.s3

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.slf4j.LoggerFactory
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ListObjectsRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.charset.Charset
import java.nio.file.Path

class S3ServiceImpl(
    private val s3AsyncClient: S3AsyncClient,
    override val bucketLocalIdentifier: AwsS3BucketLocalIdentifier,
    override val bucketName: String
) : S3Service {

    /**
     * Uploads a file to an S3 bucket.
     */
    override fun uploadFile(key: String, filePath: Path): Mono<Void> {
        val asyncRequestBodyMono = try {
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()

            Mono.just(putObjectRequest to AsyncRequestBody.fromFile(filePath))
        } catch (e: Exception) {
            Mono.error(e)
        }

        return asyncRequestBodyMono.flatMap { (putObjectRequest, asyncRequestBody) ->
            Mono.fromFuture(s3AsyncClient.putObject(putObjectRequest, asyncRequestBody))
                .doOnNext {
                    logger.info("Uploaded to S3: $key")
                }
                .onErrorResume {
                    logger.info("Failed to upload to S3: ${it.message}")
                    Mono.empty()
                }
                .then()
        }
    }

    /**
     * Uploads an object to an S3 bucket.
     */
    override fun uploadObject(key: String, obj: Any): Mono<Void> {
        val asyncRequestBodyMono = try {
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()

            Mono.just(putObjectRequest to AsyncRequestBody.fromString(mapper.writeValueAsString(obj)))
        } catch (e: Exception) {
            Mono.error(e)
        }

        return asyncRequestBodyMono.flatMap { (putObjectRequest, asyncRequestBody) ->
            Mono.fromFuture(s3AsyncClient.putObject(putObjectRequest, asyncRequestBody))
                .doOnNext {
                    logger.info("Uploaded to S3: $key")
                }
                .onErrorResume {
                    logger.info("Failed to upload to S3: ${it.message}")
                    Mono.empty()
                }
                .then()
        }
    }

    override fun getObjectText(key: String): Mono<String> {
        val requestMono = try {
            Mono.just(
                GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build()
            )
        } catch (e: Exception) {
            Mono.error(e)
        }

        return requestMono.flatMap { getObjectRequest ->
            Mono.fromFuture(
                s3AsyncClient.getObject(
                    getObjectRequest,
                    AsyncResponseTransformer.toBytes()
                )
            ).map { responseBody ->
                responseBody.asString(Charset.defaultCharset())
            }
                .doOnNext {
                    logger.info("Loaded from S3: $key")
                }
                .onErrorResume { error ->
                    logger.error("Failed to fetch from S3: ${error.message}")
                    Mono.empty()
                }
        }
    }

    override fun <T : Any> getObject(
        key: String,
        objectClass: Class<T>,
    ): Mono<T> {
        return getObjectText(key)
            .flatMap {
                try {
                    Mono.just(mapper.readValue(it, objectClass))
                } catch (e: Exception) {
                    logger.error("Error parsing item from S3: ${e.message}")
                    Mono.empty()
                }
            }
    }

    override fun listObjectKeys(
        prefix: String,
    ): Flux<String> {
        val responseMono = try {
            val listObjectsRequest = ListObjectsRequest.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build()

            Mono.fromFuture(s3AsyncClient.listObjects(listObjectsRequest))
        } catch (e: Exception) {
            Mono.error(e)
        }

        return responseMono.flatMapIterable { listObjectsResponse ->
            listObjectsResponse.contents()
                .map { s3Object ->
                    s3Object.key()
                }
        }
    }

    override fun <T: Any> loadObjects(
        objectClass: Class<T>,
        prefix: String,
    ): Flux<T> {
        return listObjectKeys(prefix)
            .concatMap { key ->
                getObject(key, objectClass)
            }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(S3Service::class.java)
        private val mapper = jacksonObjectMapper()
    }
}
