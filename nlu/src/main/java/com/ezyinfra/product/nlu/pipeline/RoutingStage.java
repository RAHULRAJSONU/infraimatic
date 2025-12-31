package com.ezyinfra.product.nlu.pipeline;

import com.ezyinfra.product.nlu.workflow.router.WorkflowRouter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class RoutingStage implements PipelineStage {

    private final WorkflowRouter router;

    public RoutingStage(WorkflowRouter router) {
        this.router = router;
    }

    @Override
    public String name() {
        return "routing-stage";
    }

    @Override
    public Mono<StageResult> process(PipelineContext ctx) {

        String response = router.route(
                ctx.getSession(),
                Map.of("Body", ctx.getText())
        );

        ctx.setResponse(response);
        return Mono.just(StageResult.stop(ctx));
    }
}

