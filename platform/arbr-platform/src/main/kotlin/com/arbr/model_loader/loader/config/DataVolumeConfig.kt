package com.arbr.model_loader.loader.config

import com.arbr.data_common.base.storage.DataVolume
import com.arbr.data_common.spec.element.DataVolumeSpec
import com.arbr.data_common.spec.model.DataStorageMedium
import com.arbr.data_common.spec.uri.DataVolumeUriComponent
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI
import java.nio.file.Paths
import kotlin.io.path.isDirectory
import kotlin.io.path.isWritable
import kotlin.io.path.notExists

@Configuration
class DataVolumeConfig {

    @Bean
    @ConditionalOnProperty(
        prefix = "arbr.model-loader",
        name = ["base-data-dir"],
        havingValue = "",
        matchIfMissing = false
    )
    fun repoDataBaseVolume(
        @Value("\${arbr.model-loader.base-data-dir}")
        baseDataDir: String,
    ): DataVolume? {
        val filePath = Paths.get(baseDataDir)

        if (filePath.notExists() || !filePath.isDirectory()) {
            logger.warn("Model loader base data dir not found or not a dir: $filePath")
            return null
        }

        val allowWrites = filePath.isWritable()
        return DataVolume.ofSpec(
            DataVolumeSpec(
                "arbr.model-loader.base-data-dir",
                "Model Loader Base Data Directory",
                DataVolumeUriComponent(filePath.toUri()),
                PRIORITY_GREATEST,
                allowWrites = allowWrites,
                DataStorageMedium.FILE_SYSTEM,
            )
        )
    }

    @Bean
    fun classpathDataVolume(): DataVolume {
        val classpathDataVolumeUri = URI("classpath:///")

        return DataVolume.ofSpec(
            DataVolumeSpec(
                "arbr.model-loader.classpath-data",
                "Model Loader Classpath Data",
                DataVolumeUriComponent(classpathDataVolumeUri),
                PRIORITY_LEAST, // Always available - use only if others not configured
                allowWrites = false,
                DataStorageMedium.FILE_SYSTEM,
            )
        )
    }

    @Bean
    @ConditionalOnProperty(
        prefix = "arbr.s3",
        name = ["bucket-datasets"],
        havingValue = "",
        matchIfMissing = false
    )
    fun s3DataVolume(
        @Value("\${arbr.s3.bucket-datasets}")
        bucketName: String
    ): DataVolume {
        val s3DataVolumeUri = URI("s3://${bucketName}/")

        return DataVolume.ofSpec(
            DataVolumeSpec(
                "arbr.model-loader.s3-data.$bucketName",
                "Model Loader S3 Data: $bucketName",
                DataVolumeUriComponent(s3DataVolumeUri),
                PRIORITY_MEDIUM,
                allowWrites = true, // TODO: Check
                DataStorageMedium.FILE_SYSTEM,
            )
        )
    }

    companion object {
        private const val PRIORITY_GREATEST = 0
        private const val PRIORITY_MEDIUM = 10
        private const val PRIORITY_LEAST = 20

        private val logger = LoggerFactory.getLogger(DataVolumeConfig::class.java)
    }

}