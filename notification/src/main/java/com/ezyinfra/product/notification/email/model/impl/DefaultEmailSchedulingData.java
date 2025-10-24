package com.ezyinfra.product.notification.email.model.impl;

import com.ezyinfra.product.common.utility.TimeUtils;
import com.ezyinfra.product.notification.email.model.Email;
import com.ezyinfra.product.notification.email.model.EmailSchedulingData;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@EqualsAndHashCode(of = {"id", "scheduledDateTime", "assignedPriority", "desiredPriority"})
@ToString(of = {
        "id",
        "scheduledDateTime",
        "assignedPriority"
})
public class DefaultEmailSchedulingData implements EmailSchedulingData {

    private static final long serialVersionUID = 60021395842232155L;
    protected final Email email;
    protected final OffsetDateTime scheduledDateTime;
    protected final int assignedPriority;
    protected final int desiredPriority;
    private final String id = UUID.randomUUID().toString();

    @Builder(builderMethodName = "defaultEmailSchedulingDataBuilder")
    public DefaultEmailSchedulingData(@NonNull final Email email,
                                      @NonNull final OffsetDateTime scheduledDateTime,
                                      final int desiredPriority,
                                      final int assignedPriority) {
        checkArgument(assignedPriority > 0, "Priority cannot be less than 1");

        this.email = email;
        this.scheduledDateTime = scheduledDateTime;
        this.desiredPriority = desiredPriority;
        this.assignedPriority = assignedPriority;
    }

    public static class DefaultEmailSchedulingDataBuilder {
        protected OffsetDateTime scheduledDateTime = TimeUtils.offsetDateTimeNow();
    }

}