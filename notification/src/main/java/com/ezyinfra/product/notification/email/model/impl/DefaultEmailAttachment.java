package com.ezyinfra.product.notification.email.model.impl;

import com.ezyinfra.product.notification.email.model.EmailAttachment;
import com.ezyinfra.product.notification.util.TikaDetector;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;

import static java.util.Optional.ofNullable;


@Slf4j
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@RequiredArgsConstructor
@ToString(exclude = "attachmentData")
public class DefaultEmailAttachment implements EmailAttachment {

    @Serial
    private static final long serialVersionUID = -3307831714212032363L;

    @Getter
    @NonNull
    private String attachmentName;

    @Getter
    @NonNull
    private byte[] attachmentData;

    private MediaType mediaType;

    public MediaType getContentType() throws IOException {
        final InputStream attachmentDataStream = new ByteArrayInputStream(attachmentData);

        final MediaType mediaType;
        try {
            mediaType = ofNullable(this.mediaType)
                    .orElse(TikaDetector.tikaDetector().detect(attachmentDataStream, attachmentName));
        } catch (IOException e) {
            log.error("The MimeType is not set. Tried to guess it but something went wrong.", e);
            throw e;
        }
        return mediaType;
    }

}