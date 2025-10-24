package com.ezyinfra.product.notification.email.model.impl;

import com.ezyinfra.product.common.utility.TimeUtils;
import com.ezyinfra.product.notification.email.model.Email;
import com.ezyinfra.product.notification.email.model.InlinePicture;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

import java.io.Serial;
import java.time.OffsetDateTime;
import java.util.Map;


@Getter
@EqualsAndHashCode(of = {"id", "scheduledDateTime", "assignedPriority", "desiredPriority"}, callSuper = false)
public class TemplateEmailSchedulingData extends DefaultEmailSchedulingData {

    @Serial
    private static final long serialVersionUID = -8267649519235191875L;

    private final String template;
    private final Map<String, Object> modelObject;
    private final InlinePicture[] inlinePictures;

    @Builder(builderMethodName = "templateEmailSchedulingDataBuilder")
    public TemplateEmailSchedulingData(@NonNull final Email email,
                                       @NonNull final OffsetDateTime scheduledDateTime,
                                       final int desiredPriority,
                                       final int assignedPriority,
                                       @NonNull final String template,
                                       @NonNull final Map<String, Object> modelObject,
                                       @NonNull final InlinePicture[] inlinePictures) {
        super(email, scheduledDateTime, desiredPriority, assignedPriority);
        this.template = template;
        this.modelObject = modelObject;
        this.inlinePictures = inlinePictures;
    }

    public static class TemplateEmailSchedulingDataBuilder {
        protected OffsetDateTime scheduledDateTime = TimeUtils.offsetDateTimeNow();
    }

}