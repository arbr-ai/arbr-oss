package com.arbr.platform.object_graph.concurrency

/**
 * Required level of lock privileges
 * Note that ordinal is significant for when an upgrade is needed
 */
enum class LockLevel {
    READ, WRITE;
}