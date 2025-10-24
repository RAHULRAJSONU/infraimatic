package com.ezyinfra.product.notification.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

public class MimeMessageHelperExt extends MimeMessageHelper {

    private static final String HEADER_RETURN_RECEIPT = "Return-Receipt-To";

    private static final String HEADER_DEPOSITION_NOTIFICATION_TO = "Disposition-Notification-To";

    public MimeMessageHelperExt(MimeMessage mimeMessage) {
        super(mimeMessage);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, String encoding) {
        super(mimeMessage, encoding);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, boolean multipart) throws MessagingException {
        super(mimeMessage, multipart);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, boolean multipart, String encoding) throws MessagingException {
        super(mimeMessage, multipart, encoding);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, int multipartMode) throws MessagingException {
        super(mimeMessage, multipartMode);
    }

    public MimeMessageHelperExt(MimeMessage mimeMessage, int multipartMode, String encoding) throws MessagingException {
        super(mimeMessage, multipartMode, encoding);
    }

    public void setHeaderReturnReceipt(String emailToNotification) throws MessagingException {
        getMimeMessage().setHeader(HEADER_RETURN_RECEIPT, emailToNotification);
    }

    public void setHeaderDepositionNotificationTo(String emailToNotification) throws MessagingException {
        getMimeMessage().setHeader(HEADER_DEPOSITION_NOTIFICATION_TO, emailToNotification);
    }

}