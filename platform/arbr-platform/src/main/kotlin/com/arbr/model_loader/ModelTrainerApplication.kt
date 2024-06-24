package com.arbr.model_loader

import com.arbr.model_loader.training.PatchEditDistanceOptimizer
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ModelTrainerApplication

fun main(args: Array<String>) {
    val context = runApplication<ModelTrainerApplication>(*args)

    val logger = LoggerFactory.getLogger(ModelTrainerApplication::class.java)
    logger.info("Optimizing patch alignment weights")

    val patchEditDistanceOptimizer = context.getBean(PatchEditDistanceOptimizer::class.java)
    patchEditDistanceOptimizer.optimize()
}
