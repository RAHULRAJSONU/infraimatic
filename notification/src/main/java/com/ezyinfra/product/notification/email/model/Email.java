package com.ezyinfra.product.notification.email.model;

import jakarta.mail.internet.InternetAddress;
import lombok.NonNull;

import java.io.Serializable;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.EMPTY;

public interface Email extends Serializable {

    @NonNull
    InternetAddress getFrom();

    InternetAddress getReplyTo();

    @NonNull
    Collection<InternetAddress> getTo();

    @NonNull
    default Collection<InternetAddress> getCc() {
        return List.of();
    }

    @NonNull
    default Collection<InternetAddress> getBcc() {
        return List.of();
    }

    @NonNull
    default String getSubject() {
        return EMPTY;
    }

    @NonNull
    default String getBody() {
        return EMPTY;
    }

    @NonNull
    default Collection<EmailAttachment> getAttachments() {
        return List.of();
    }

    /**
     * Return the charset encoding. Default value is UTF-8
     */
    //Observe that Charset does not guarantee that the object is Serializable, therefore we may break serialization
    String getEncoding();

    Locale getLocale();

    Date getSentAt();

    void setSentAt(Date sentAt);

    InternetAddress getReceiptTo();

    InternetAddress getDepositionNotificationTo();

    @NonNull
    default Map<String, String> getCustomHeaders() {
        return Map.of();
    }

}