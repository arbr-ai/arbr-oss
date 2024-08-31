package com.arbr.object_model.store

import com.arbr.platform.object_graph.types.ArbrForeignKey
import com.arbr.platform.object_graph.store.ResourceKVStoreProvider
import com.arbr.platform.object_graph.store.ResourceKVStoreProviderImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// TODO: Templatize
@Configuration
class ArbrKVStoreProviderConfig {

    @Bean
    fun kvStoreProvider(): ResourceKVStoreProvider<ArbrForeignKey> {
        return ResourceKVStoreProviderImpl(ArbrKVStore.load())
    }
}
