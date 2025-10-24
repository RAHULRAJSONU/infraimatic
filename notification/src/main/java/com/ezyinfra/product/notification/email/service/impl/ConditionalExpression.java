package com.ezyinfra.product.notification.email.service.impl;

import lombok.experimental.UtilityClass;

import static com.ezyinfra.product.notification.email.config.ApplicationPropertiesConstants.*;

@UtilityClass
public class ConditionalExpression {

    public static final String SCHEDULER_IS_ENABLED = "'${" + SPRING_MAIL_SCHEDULER_ENABLED + ":false}' == 'true'";

    public static final String PERSISTENCE_IS_ENABLED = SCHEDULER_IS_ENABLED + " && '${" + SPRING_MAIL_PERSISTENCE_ENABLED + ":false}' == 'true'";

    public static final String PERSISTENCE_IS_ENABLED_WITH_REDIS =
            PERSISTENCE_IS_ENABLED +
                    " && '${" + SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_ENABLED + ":false}' == 'true'";

    public static final String PERSISTENCE_IS_ENABLED_WITH_EMBEDDED_REDIS =
            PERSISTENCE_IS_ENABLED +
                    " && '${" + SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_ENABLED + ":false}' == 'true'" +
                    " && '${" + SPRING_MAIL_SCHEDULER_PERSISTENCE_REDIS_EMBEDDED + ":false}' == 'true'";


    public static final String EMAIL_LOGGING_RENDERER_IS_ENABLED = "'${" + SPRING_MAIL_LOGGING_ENABLED + ":true}' == 'true'";

}
