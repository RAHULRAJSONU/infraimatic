package com.ezyinfra.product.nlu.pipeline.whatsapp;

import com.ezyinfra.product.nlu.pipeline.PipelineContext;
import com.ezyinfra.product.nlu.pipeline.PipelineStage;
import com.ezyinfra.product.nlu.pipeline.StageStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class WhatsAppPipeline {

    private final List<PipelineStage> stages;

    public WhatsAppPipeline(List<PipelineStage> stages) {
        this.stages = stages;
    }

    public Mono<String> execute(PipelineContext ctx) {

        Mono<PipelineContext> flow = Mono.just(ctx);

        for (PipelineStage stage : stages) {
            flow = flow.flatMap(current ->
                    stage.process(current)
                            .doOnSubscribe(s ->
                                    log.debug("[{}] start stage={}", current.getRequestId(), stage.name()))
                            .flatMap(result -> {
                                if (result.status() == StageStatus.CONTINUE) {
                                    return Mono.just(result.context());
                                }
                                if (result.status() == StageStatus.STOP) {
                                    return Mono.just(result.context());
                                }
                                return Mono.error(
                                        new IllegalStateException("Pipeline error in stage " + stage.name())
                                );
                            })
            );
        }

        return flow
                .map(PipelineContext::getResponse)
                .timeout(Duration.ofSeconds(20))
                .onErrorResume(ex -> {
                    log.error("[{}] pipeline failed", ctx.getRequestId(), ex);
                    return Mono.just("❌ Something went wrong. Please try again.");
                });
    }
}

