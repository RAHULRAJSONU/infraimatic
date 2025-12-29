package com.ezyinfra.product.checkpost.identity.pipeline;

public interface WebhookPipelineStep {

    boolean supports(WebhookType type);

    void execute(WebhookContext context);
}
