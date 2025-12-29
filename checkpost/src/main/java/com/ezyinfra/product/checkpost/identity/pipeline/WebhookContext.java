package com.ezyinfra.product.checkpost.identity.pipeline;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class WebhookContext {

    private final WebhookType type;
    private final HttpServletRequest request;

    @Setter
    private String senderMobile;
    @Setter
    private String tenantId;

    public WebhookContext(WebhookType type, HttpServletRequest request) {
        this.type = type;
        this.request = request;
    }

}
