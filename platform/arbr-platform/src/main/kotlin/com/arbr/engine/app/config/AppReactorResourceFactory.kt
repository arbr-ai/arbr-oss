package com.arbr.engine.app.config

import org.springframework.context.annotation.Primary
import org.springframework.http.client.ReactorResourceFactory
import org.springframework.stereotype.Component

@Primary
@Component
class AppReactorResourceFactory: ReactorResourceFactory() {

    @Override
    override fun stop() {
        try {
            super.stop()
        } catch (e: Exception) {
            // Ignore
        }
    }

    @Override
    override fun destroy() {
        try {
            super.destroy()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
