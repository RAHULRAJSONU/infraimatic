package com.ezyinfra.product.common.exception;

public record ApiErrorResponse(
        int errorCode,
        String description) {

}