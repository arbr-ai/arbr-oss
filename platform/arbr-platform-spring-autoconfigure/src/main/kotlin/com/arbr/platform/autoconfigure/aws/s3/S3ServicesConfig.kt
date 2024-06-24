package com.arbr.platform.autoconfigure.aws.s3

import com.arbr.aws.s3.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnMissingBean(S3Services::class)
class S3ServicesConfig {

    @Bean
    fun s3ServiceList(
        @Autowired(required = false)
        s3ServiceFactory: S3ServiceFactory?,
        s3BucketConfigs: List<AwsS3BucketConfig>,
    ): List<S3Service> {
        return s3ServiceFactory?.let { serviceFactory ->
            s3BucketConfigs.map {
                serviceFactory.makeS3Service(it)
            }
        } ?: emptyList()
    }

    @Bean
    fun s3Services(
        s3ServiceList: List<S3Service>
    ): S3Services {
        return S3ServicesImpl(s3ServiceList)
    }
}
