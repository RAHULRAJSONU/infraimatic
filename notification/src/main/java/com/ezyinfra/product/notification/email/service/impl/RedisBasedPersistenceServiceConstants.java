package com.ezyinfra.product.notification.email.service.impl;

import com.google.common.base.Preconditions;
import lombok.experimental.UtilityClass;

@UtilityClass
public class RedisBasedPersistenceServiceConstants {

    public static final String ORDERING_KEY_PREFIX = "priority-level:";

    public static final String orderingKey(final int priorityLevel) {
        Preconditions.checkArgument(priorityLevel > 0, "Priority level must be a positive integer number");
        return orderingKeyPrefix() + priorityLevel;
    }

    public static final String orderingKeyPrefix() {
        return ORDERING_KEY_PREFIX;
    }

}