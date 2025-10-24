package com.ezyinfra.product.notification.email.model.impl;

import com.ezyinfra.product.notification.email.model.ImageType;
import com.ezyinfra.product.notification.email.model.InlinePicture;
import lombok.*;

import java.io.File;
import java.io.Serial;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultInlinePicture implements InlinePicture {

    @Serial
    private static final long serialVersionUID = 1040548679790587446L;
    @NonNull
    byte[] imageBytes;
    @NonNull
    private ImageType imageType;
    private File file;
    @NonNull
    private String templateName;

}