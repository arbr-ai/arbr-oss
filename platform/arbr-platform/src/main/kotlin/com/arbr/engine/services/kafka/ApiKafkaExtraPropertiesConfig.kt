package com.arbr.engine.services.kafka

import com.arbr.kafka.topic.base.ApiKafkaExtraProperties
import org.apache.kafka.common.config.SaslConfigs
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApiKafkaExtraPropertiesConfig(
    @Value("\${arbr.kafka.use_sasl:true}")
    private val useSasl: Boolean,
) {

    @Bean
    fun extraProperties(): ApiKafkaExtraProperties {
        val props = mutableMapOf<String, Any>()
        if (useSasl) {
            props[SaslConfigs.SASL_MECHANISM] = "OAUTHBEARER"
            props[SaslConfigs.SASL_JAAS_CONFIG] =
                "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;"
            props[SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS] =
                "software.amazon.msk.auth.iam.IAMOAuthBearerLoginCallbackHandler"
        }

        return ApiKafkaExtraProperties(props)
    }

}
