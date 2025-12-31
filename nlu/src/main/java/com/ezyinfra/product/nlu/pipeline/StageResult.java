package com.ezyinfra.product.nlu.pipeline;

public record StageResult(
        StageStatus status,
        PipelineContext context
) {
    public static StageResult next(PipelineContext ctx) {
        return new StageResult(StageStatus.CONTINUE, ctx);
    }
    public static StageResult stop(PipelineContext ctx) {
        return new StageResult(StageStatus.STOP, ctx);
    }
    public static StageResult error(PipelineContext ctx) {
        return new StageResult(StageStatus.ERROR, ctx);
    }
}
