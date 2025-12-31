package com.ezyinfra.product.nlu.pipeline;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class ResponseStage implements PipelineStage {

    @Override
    public String name() {
        return "response-stage";
    }

    @Override
    public Mono<StageResult> process(PipelineContext ctx) {
        // Final stage – response already prepared
        return Mono.just(StageResult.stop(ctx));
    }
}