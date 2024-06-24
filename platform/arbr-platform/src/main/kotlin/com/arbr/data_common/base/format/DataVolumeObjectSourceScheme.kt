package com.arbr.data_common.base.format

import com.arbr.data_common.spec.uri.DataVolumeUriScheme

sealed interface DataVolumeObjectSourceScheme {
    val scheme: DataVolumeUriScheme

    /**
     * Local file system source
     */
    data object LocalFileSystem : DataVolumeObjectSourceScheme {
        override val scheme: DataVolumeUriScheme = DataVolumeUriScheme.FILE
    }

    /**
     * Local classpath source
     */
    data object Classpath : DataVolumeObjectSourceScheme {
        override val scheme: DataVolumeUriScheme = DataVolumeUriScheme.FILE
    }

    /**
     * Memory source
     */
    data object Memory : DataVolumeObjectSourceScheme {
        override val scheme: DataVolumeUriScheme = DataVolumeUriScheme.MEMORY
    }

    /**
     * S3 source
     */
    data object S3 : DataVolumeObjectSourceScheme {
        override val scheme: DataVolumeUriScheme = DataVolumeUriScheme.S3
    }

    /**
     * Database source with JDBC scheme
     */
    data object DatabaseJdbc : DataVolumeObjectSourceScheme {
        override val scheme: DataVolumeUriScheme = DataVolumeUriScheme.JDBC
    }

    /**
     * Database source with R2DBC scheme
     */
    data object DatabaseR2dbc : DataVolumeObjectSourceScheme {
        override val scheme: DataVolumeUriScheme = DataVolumeUriScheme.R2DBC
    }

    companion object {
        fun forScheme(
            scheme: DataVolumeUriScheme,
        ): DataVolumeObjectSourceScheme {
            return when (scheme) {
                DataVolumeUriScheme.FILE -> LocalFileSystem
                DataVolumeUriScheme.CLASSPATH -> Classpath
                DataVolumeUriScheme.MEMORY -> Memory
                DataVolumeUriScheme.S3 -> S3
                DataVolumeUriScheme.JDBC -> DatabaseJdbc
                DataVolumeUriScheme.R2DBC -> DatabaseR2dbc
            }
        }
    }
}