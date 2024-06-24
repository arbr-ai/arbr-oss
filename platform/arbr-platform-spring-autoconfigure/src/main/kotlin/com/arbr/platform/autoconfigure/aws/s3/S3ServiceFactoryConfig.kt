package com.arbr.platform.autoconfigure.aws.s3

import com.arbr.aws.s3.S3ServiceFactory
import com.arbr.aws.s3.S3ServiceFactoryImpl
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsCredentials

@Configuration
@ConditionalOnMissingBean(S3ServiceFactory::class)
class S3ServiceFactoryConfig {

    @Bean
    fun s3ServiceFactory(
        @Autowired(required = false)
        awsCredentials: AwsCredentials?
    ): S3ServiceFactory? {
        return awsCredentials?.let {
            S3ServiceFactoryImpl(it)
        }
    }
}
