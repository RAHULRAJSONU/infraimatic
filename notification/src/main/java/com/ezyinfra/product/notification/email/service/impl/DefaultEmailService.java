package com.ezyinfra.product.notification.email.service.impl;

import com.ezyinfra.product.common.exception.CannotSendEmailException;
import com.ezyinfra.product.common.exception.TemplateException;
import com.ezyinfra.product.notification.email.model.Email;
import com.ezyinfra.product.notification.email.model.EmailAttachment;
import com.ezyinfra.product.notification.email.model.InlinePicture;
import com.ezyinfra.product.notification.email.service.EmailService;
import com.ezyinfra.product.notification.email.service.TemplateService;
import com.ezyinfra.product.notification.util.EmailToMimeMessage;
import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;
import jakarta.mail.util.ByteArrayDataSource;
import jakarta.validation.constraints.NotNull;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class DefaultEmailService implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateService templateService;
    private final EmailToMimeMessage emailToMimeMessage;

    @Autowired(required = false)
    public DefaultEmailService(final @NonNull JavaMailSender javaMailSender,
                               final TemplateService templateService,
                               final @NonNull EmailToMimeMessage emailToMimeMessage) {
        this.javaMailSender = javaMailSender;
        this.templateService = templateService;
        this.emailToMimeMessage = emailToMimeMessage;
    }

    @Autowired(required = false)
    public DefaultEmailService(final @NonNull JavaMailSender javaMailSender,
                               final @NonNull EmailToMimeMessage emailToMimeMessage) {
        this(javaMailSender, null, emailToMimeMessage);
    }

    @Override
    public MimeMessage send(final @NonNull Email email) {
        email.setSentAt(new Date());
        final MimeMessage mimeMessage = toMimeMessage(email);
        javaMailSender.send(mimeMessage);
        return mimeMessage;
    }

    @Override
    public MimeMessage send(final @NonNull Email email,
                            final @NonNull String template,
                            final Map<String, Object> modelObject,
                            final @NonNull InlinePicture... inlinePictures) throws CannotSendEmailException {
        email.setSentAt(new Date());
        final MimeMessage mimeMessage = toMimeMessage(email);
        try {
            final MimeMultipart content = new MimeMultipart("mixed");

            String text = templateService.mergeTemplateIntoString(template,
                    Optional.ofNullable(modelObject).orElse(Map.of()));

            for (final InlinePicture inlinePicture : inlinePictures) {
                final String cid = UUID.randomUUID().toString();

                // Set the cid in the template
                text = text.replace(inlinePicture.getTemplateName(), "cid:" + cid);

                // Set the image part using byte[]
                final MimeBodyPart imagePart = new MimeBodyPart();
                DataSource imageSource = new ByteArrayDataSource(inlinePicture.getImageBytes(),
                        inlinePicture.getImageType().getContentType());
                imagePart.setDataHandler(new DataHandler(imageSource));
                imagePart.setContentID('<' + cid + '>');
                imagePart.setDisposition(MimeBodyPart.INLINE);
                imagePart.setHeader("Content-Type", inlinePicture.getImageType().getContentType());
                content.addBodyPart(imagePart);
            }

            for (final EmailAttachment emailAttachment : email.getAttachments()) {
                // Set the attachment part using byte[]
                final MimeBodyPart attachmentPart = new MimeBodyPart();
                DataSource source = new ByteArrayDataSource(emailAttachment.getAttachmentData(),
                        emailAttachment.getContentType().toString());
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(MimeUtility.encodeText(emailAttachment.getAttachmentName()));
                content.addBodyPart(attachmentPart);
            }

            // Set the HTML text part
            final MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(text, email.getEncoding(), "html");
            content.addBodyPart(textPart);

            mimeMessage.setContent(content);
            mimeMessage.saveChanges();
            javaMailSender.send(mimeMessage);
            log.info("Sent email {}.", emailWithCompiledBody(email, text));
        } catch (IOException e) {
            log.error("The template file cannot be read", e);
            throw new CannotSendEmailException("Error while sending the email due to problems with the template file.", e);
        } catch (TemplateException e) {
            log.error("The template file cannot be processed", e);
            throw new CannotSendEmailException("Error while processing the template file with the given model object.", e);
        } catch (MessagingException e) {
            log.error("The mime message cannot be created", e);
            throw new CannotSendEmailException("Error while sending the email due to problems with the mime content.", e);
        }
        return mimeMessage;
    }

    @Override
    public MimeMessage sendWithInLineImage(final @NonNull Email email,
                                           final @NonNull String template,
                                           final Map<String, Object> modelObject,
                                           final @NonNull InlinePicture inlinePicture) throws CannotSendEmailException {
        email.setSentAt(new Date());
        final MimeMessage mimeMessage = toMimeMessage(email);
        try {
            // Create a helper with multipart mode
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED, "UTF-8");

            // Merge template with model
            String htmlContent = templateService.mergeTemplateIntoString(template,
                    Optional.ofNullable(modelObject).orElse(Map.of()));

            // Generate a unique CID
            String cid = UUID.randomUUID().toString();

            // Replace placeholder with CID reference
            htmlContent = htmlContent.replace(inlinePicture.getTemplateName(), "cid:" + cid);

            // Set the HTML content with embedded image reference
            message.setText(htmlContent, true);  // true indicates it's HTML

            // Attach the inline image using byte[]
            final InputStreamSource imageSource = new ByteArrayResource(inlinePicture.getImageBytes());
            message.addInline(cid, imageSource, inlinePicture.getImageType().getContentType());

            // Send the email
            javaMailSender.send(mimeMessage);
            log.info("Sent email {}.", emailWithCompiledBody(email, htmlContent));
        } catch (IOException e) {
            log.error("The template file cannot be read", e);
            throw new CannotSendEmailException("Error while sending the email due to problems with the template file.", e);
        } catch (TemplateException e) {
            log.error("The template file cannot be processed", e);
            throw new CannotSendEmailException("Error while processing the template file with the given model object.", e);
        } catch (MessagingException e) {
            log.error("The mime message cannot be created", e);
            throw new CannotSendEmailException("Error while sending the email due to problems with the mime content.", e);
        }
        return mimeMessage;
    }


    private MimeMessage toMimeMessage(@NotNull Email email) {
        return emailToMimeMessage.apply(email);
    }

    private Email emailWithCompiledBody(Email email, String body) {
        return new EmailFromTemplate(email).body(body);
    }

    @RequiredArgsConstructor
    @Accessors(fluent = true)
    private static class EmailFromTemplate implements Email {
        @Delegate
        private final Email email;

        @Setter
        private String body;
    }
}
