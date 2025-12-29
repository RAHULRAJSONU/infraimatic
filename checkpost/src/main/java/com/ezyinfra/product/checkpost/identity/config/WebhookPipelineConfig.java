package com.ezyinfra.product.checkpost.identity.config;

import com.ezyinfra.product.checkpost.identity.pipeline.*;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebhookPipelineConfig {

    @Bean
    public WebhookPipeline twilioWebhookPipeline(ApplicationContext applicationContext) {

        return new WebhookPipeline(applicationContext)
                .register(TwilioSignatureStep.class)
                .then(TwilioWebhookClassifierStep.class)
                .then(TwilioSenderExtractionStep.class)
                .then(TenantResolutionStep.class);
    }
}
