package com.arbr.object_model.store

import com.arbr.object_model.core.types.ArbrForeignKey
import com.arbr.og.store.ResourceKVStoreProvider
import com.arbr.og.store.ResourceKVStoreProviderImpl
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
