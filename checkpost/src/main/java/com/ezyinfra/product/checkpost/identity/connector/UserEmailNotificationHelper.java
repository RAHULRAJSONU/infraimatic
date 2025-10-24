package com.ezyinfra.product.checkpost.identity.connector;

import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.notification.email.config.MailProperties;
import com.ezyinfra.product.notification.email.model.Email;
import com.ezyinfra.product.notification.email.model.EmailTemplate;
import com.ezyinfra.product.notification.email.model.ImageType;
import com.ezyinfra.product.notification.email.model.InlinePicture;
import com.ezyinfra.product.notification.email.model.impl.DefaultEmail;
import com.ezyinfra.product.notification.email.model.impl.DefaultInlinePicture;
import com.ezyinfra.product.notification.email.service.EmailService;
import jakarta.mail.internet.InternetAddress;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEmailNotificationHelper {

    private final EmailService emailService;
    private final MailProperties mailProperties;
    private final ResourceLoader resourceLoader;

    public void notifyUser(User user, EmailTemplate template, Map<String, Object> context) {
        try {
            final Email email = DefaultEmail.builder()
                    .from(new InternetAddress(mailProperties.getUsername(), "NexusX Identity Service"))
                    .to(List.of(
                            new InternetAddress(user.getEmail(), user.getName())))
                    .subject(template.getSubject())
                    .body("")
                    .encoding("UTF-8").build();
            emailService.send(email, template.getTemplate(), context, createInlinePicture());
        } catch (Exception e) {
            log.error("Could not send the email, error: {}", e.getMessage());
        }
    }

    private InlinePicture createInlinePicture() throws URISyntaxException, IOException {
        Resource resource = resourceLoader.getResource("classpath:images/nexusx-logo.png");
        InputStream inputStream = resource.getInputStream();
        return DefaultInlinePicture.builder()
                .imageBytes(inputStream.readAllBytes())
                .imageType(ImageType.PNG)
                .templateName("nexusx-logo").build();
    }
}
