package com.arbr.core_web_dev

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(
    exclude = [
        R2dbcAutoConfiguration::class,
    ],
)
class PlatformWebDevMain

fun main(args: Array<String>) {
    val context = runApplication<PlatformWebDevMain>(*args)
    println("Hello world\n$context")
}
