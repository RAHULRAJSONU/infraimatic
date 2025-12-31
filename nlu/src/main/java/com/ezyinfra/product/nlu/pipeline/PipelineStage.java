package com.ezyinfra.product.nlu.pipeline;

import reactor.core.publisher.Mono;

public interface PipelineStage {

    String name();

    Mono<StageResult> process(PipelineContext ctx);
}
