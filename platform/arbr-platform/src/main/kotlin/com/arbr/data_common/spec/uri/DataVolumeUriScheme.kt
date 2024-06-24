package com.arbr.data_common.spec.uri

enum class DataVolumeUriScheme(val prefix: String) {
    /**
     * A file on a locally-mounted disk
     */
    FILE("file"),

    /**
     * A resource in the classpath.
     */
    CLASSPATH("classpath"),

    /**
     * A resource in memory.
     */
    MEMORY("memory"),

    /**
     * S3
     */
    S3("s3"),

    /**
     * JDBC, i.e., a database with a jdbc protocol connection
     */
    JDBC("jdbc"),

    /**
     * JDBC, i.e., a database with an r2dbc protocol connection
     */
    R2DBC("r2dbc"),
}