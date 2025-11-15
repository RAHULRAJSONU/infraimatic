package com.ezyinfra.product.nlu.service;

import java.util.Map;

public interface LLMClient {
    String complete(String prompt, Map<String, Object> options);
}
