package com.ezyinfra.product.notification.email.model;

import lombok.Getter;

@Getter
public enum EmailTemplate {

    IDENTITY_ACCOUNT_CREATED("identity-account-created", "User Account Created Successfully!"),
    IDENTITY_PASSWORD_CHANGE_SUCCESS("identity-password-change-success", "Ezyinfra Account password changed"),
    IDENTITY_PASSWORD_RESET_REQUEST("identity-password-reset-request", "Please reset your password."),
    IDENTITY_PASSWORD_RESET_SUCCESS("identity-password-reset-success", "Password Reset Successfully.");

    private final String template;
    private final String subject;

    EmailTemplate(String template, String subject) {
        this.template = template;
        this.subject = subject;
    }

    @Override
    public String toString() {
        return template;
    }
}
