package com.ezyinfra.product.notification.email.service.impl;

import com.ezyinfra.product.common.exception.TemplateException;
import com.ezyinfra.product.notification.email.service.TemplateService;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.io.Files.getFileExtension;

@Service
public class ThymeleafTemplateService implements TemplateService {

    @Autowired
    private SpringTemplateEngine thymeleafEngine;

    @Value("${spring.thymeleaf.suffix:.html}")
    private String thymeleafSuffix;

    @Override
    @NonNull
    public String mergeTemplateIntoString(final @NonNull String emailTemplate,
                                          final @NonNull Map<String, Object> model)
            throws IOException, TemplateException {
        final String trimmedTemplateReference = emailTemplate.toString();
        checkArgument(!isNullOrEmpty(trimmedTemplateReference), "The given template is null, empty or blank");
        if (trimmedTemplateReference.contains("."))
            checkArgument(Objects.equals(getNormalizedFileExtension(trimmedTemplateReference), expectedTemplateExtension()),
                    "Expected a Thymeleaf template file with extension '%s', while '%s' was given. To check " +
                            "the default extension look at 'spring.thymeleaf.suffix' in your application.properties file",
                    expectedTemplateExtension(), getNormalizedFileExtension(trimmedTemplateReference));

        final Context context = new Context();
        context.setVariables(model);

        return thymeleafEngine.process(FilenameUtils.removeExtension(trimmedTemplateReference), context);
    }

    @Override
    public String expectedTemplateExtension() {
        return thymeleafSuffix;
    }

    private String getNormalizedFileExtension(final String templateReference) {
        return "." + getFileExtension(templateReference);
    }

}