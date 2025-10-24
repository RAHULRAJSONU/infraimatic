package com.ezyinfra.product.notification.email.model.impl;

import com.ezyinfra.product.notification.email.model.Email;
import com.ezyinfra.product.notification.email.model.EmailAttachment;
import jakarta.mail.internet.InternetAddress;
import lombok.*;

import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.*;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
public class DefaultEmail implements Email {

    @Serial
    private static final long serialVersionUID = 634175529482595823L;

    @NonNull
    private InternetAddress from;

    private InternetAddress replyTo;

    private Collection<InternetAddress> to;

    private Collection<InternetAddress> cc;

    private Collection<InternetAddress> bcc;

    @NonNull
    private String subject;

    @NonNull
    private String body;

    @NonNull
    @Singular
    private Collection<EmailAttachment> attachments;

    private String encoding = StandardCharsets.UTF_8.name();

    private Locale locale;

    private Date sentAt;

    private InternetAddress receiptTo;

    private InternetAddress depositionNotificationTo;

    private Map<String, String> customHeaders;

    //This is to have default values in Lombok constructor
    public static class DefaultEmailBuilder {

        private Collection<InternetAddress> cc = List.of();

        private Collection<InternetAddress> bcc = List.of();

        private String encoding = StandardCharsets.UTF_8.name();

        private Map<String, String> customHeaders = Map.of();
    }

}