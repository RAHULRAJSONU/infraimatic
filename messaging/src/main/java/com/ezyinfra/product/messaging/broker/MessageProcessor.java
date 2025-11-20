package com.ezyinfra.product.messaging.broker;

import java.util.Map;

@FunctionalInterface
public interface MessageProcessor {
    void process(Map<String, String> payload) throws Exception;
}