package com.arbr.core_web_dev.config
//
//import com.arbr.engine.services.workflow.state.WorkflowResourceLifecycleService
//import com.arbr.object_model.core.resource.ArbrRoot
//import com.arbr.platform.object_graph.impl.ObjectModelResource
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//
//@Configuration
//class WorkflowResourceLifecycleConfiguration {
//
//    @Bean
//    fun workflowResourceLifecycleService(): WorkflowResourceLifecycleService {
//        return object : WorkflowResourceLifecycleService {
//            override fun hasStrongPathToProjectOwner(
//                resource: ObjectModelResource<*, *, *>,
//            ): Boolean {
//                val targetClass = ArbrRoot::class.java
//
//                if (resource is ArbrRoot) {
//                    return true
//                }
//
//                // BFS for a match
//                val resources = listOf(resource)
//                val seen = resources.map { it.uuid }.toMutableSet()
//                var frontier = resources.flatMap { r ->
//                    r.getForeignKeys().values.flatMap {
//                        listOfNotNull(it.getLatestValue()?.resource(), it.getLatestAcceptedValue()?.resource())
//                    }
//                }
//                    .distinctBy { it.uuid }
//                val maxRounds = 8
//
//                var i = 0
//                while (i < maxRounds && frontier.isNotEmpty()) {
//                    seen.addAll(frontier.map { it.uuid })
//
//                    val targetResources = frontier
//                        .filterIsInstance(targetClass)
//
//                    if (targetResources.isNotEmpty()) {
//                        return true
//                    }
//
//                    frontier = frontier
//                        .flatMap { r ->
//                            r.getForeignKeys().values.flatMap {
//                                listOfNotNull(it.getLatestValue()?.resource(), it.getLatestAcceptedValue()?.resource())
//                            }
//                        }
//                        .filter { it.uuid !in seen }
//                        .distinctBy { it.uuid }
//                    i++
//                }
//
//                return false
//            }
//        }
//    }
//
//}
