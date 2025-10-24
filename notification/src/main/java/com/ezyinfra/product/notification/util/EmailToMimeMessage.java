package com.ezyinfra.product.notification.util;

import com.ezyinfra.product.common.exception.EmailConversionException;
import com.ezyinfra.product.notification.email.model.Email;
import com.ezyinfra.product.notification.email.model.EmailAttachment;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.base.Optional.fromNullable;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@Component
@Slf4j
public class EmailToMimeMessage implements Function<Email, MimeMessage> {

    private final JavaMailSender javaMailSender;

    @Autowired
    public EmailToMimeMessage(final JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    @Override
    public MimeMessage apply(final Email email) {
        final MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        final boolean isMultipart = nonNull(email.getAttachments()) && !email.getAttachments().isEmpty();

        try {
            final MimeMessageHelperExt messageHelper = new MimeMessageHelperExt(mimeMessage,
                    isMultipart,
                    fromNullable(email.getEncoding()).or(StandardCharsets.UTF_8.name()));

            messageHelper.setFrom(email.getFrom());
            if (nonNull(email.getReplyTo())) {
                messageHelper.setReplyTo(email.getReplyTo());
            }
            if (nonNull(email.getTo())) {
                for (final InternetAddress address : email.getTo()) {
                    messageHelper.addTo(address);
                }
            }
            if (nonNull(email.getCc())) {
                for (final InternetAddress address : email.getCc()) {
                    messageHelper.addCc(address);
                }
            }
            if (nonNull(email.getBcc())) {
                for (final InternetAddress address : email.getBcc()) {
                    messageHelper.addBcc(address);
                }
            }
            if (isMultipart) {
                for (final EmailAttachment attachment : email.getAttachments()) {
                    messageHelper.addAttachment(attachment.getAttachmentName(), attachment.getInputStream());
                }
            }
            messageHelper.setSubject(Optional.of(email.getSubject()).orElse(EMPTY));
            messageHelper.setText(Optional.of(email.getBody()).orElse(EMPTY));

            if (nonNull(email.getSentAt())) {
                messageHelper.setSentDate(email.getSentAt());
            }

            if (nonNull(email.getReceiptTo())) {
                messageHelper.setHeaderReturnReceipt(email.getReceiptTo().getAddress());
            }

            if (nonNull(email.getDepositionNotificationTo())) {
                messageHelper.setHeaderDepositionNotificationTo(email.getDepositionNotificationTo().getAddress());
            }

            if (nonNull(email.getCustomHeaders()) && !email.getCustomHeaders().isEmpty()) {
                setCustomHeaders(email, mimeMessage);
            }

            mimeMessage.saveChanges();
        } catch (MessagingException e) {
            log.error("Error while converting DefaultEmail to MimeMessage");
            throw new EmailConversionException(e);
        }

        return mimeMessage;
    }

    protected void setCustomHeaders(Email email, MimeMessage mimeMessage) {
        email.getCustomHeaders().forEach((key, value) -> {
            try {
                mimeMessage.setHeader(key, value);
            } catch (MessagingException e) {
                log.warn("Exception while setting custom email header with value {} and key {}. " +
                        "The MimeEmail will be created anyway but something may go wrong afterward.", key, value, e);
            }
        });
    }

}