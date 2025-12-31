package com.ezyinfra.product.nlu.pipeline;

public enum StageStatus {
    CONTINUE,     // move to next stage
    STOP,         // stop pipeline, response already set
    ERROR         // unrecoverable error
}
