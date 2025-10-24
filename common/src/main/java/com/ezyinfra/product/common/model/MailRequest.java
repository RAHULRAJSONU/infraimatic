package com.ezyinfra.product.common.model;

import java.util.Map;

public record MailRequest(String userEmail, String subject, String cc, boolean html, Map<String, String> context,
                          String template) {
}
