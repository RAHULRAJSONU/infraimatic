package com.ezyinfra.product.checkpost.identity.pipeline;

import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

public class WebhookPipeline {

    private final ApplicationContext applicationContext;
    private final List<WebhookPipelineStep> steps = new ArrayList<>();

    public WebhookPipeline(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public WebhookPipeline register(Class<? extends WebhookPipelineStep> stepType) {
        WebhookPipelineStep step = applicationContext.getBean(stepType);

        if (steps.contains(step)) {
            throw new IllegalStateException(
                    "Pipeline step already registered: " + stepType.getSimpleName()
            );
        }

        steps.add(step);
        return this;
    }

    public WebhookPipeline then(Class<? extends WebhookPipelineStep> stepType) {
        return register(stepType);
    }

    public void process(WebhookContext context) {
        try {
            for (WebhookPipelineStep step : steps) {
                if (step.supports(context.getType())) {
                    step.execute(context);
                }
            }
        }catch (Exception _){

        }
    }

}

