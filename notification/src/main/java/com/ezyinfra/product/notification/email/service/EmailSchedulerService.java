package com.ezyinfra.product.notification.email.service;

import com.ezyinfra.product.common.exception.CannotSendEmailException;
import com.ezyinfra.product.notification.email.model.Email;
import com.ezyinfra.product.notification.email.model.InlinePicture;
import com.ezyinfra.product.notification.email.model.ServiceStatus;
import org.springframework.scheduling.annotation.Async;

import java.time.OffsetDateTime;
import java.util.Map;

public interface EmailSchedulerService {

    /**
     * Schedules the sending of an email message at time <strong>now</strong> (UTC).
     *
     * @param mimeEmail            an email to be sent
     * @param desiredPriorityLevel the desiredPriority level for the email:
     *                             the emails with scheduledTime<=now are sent according to an order depending
     *                             on their desiredPriority level
     */
    @Async
    void schedule(Email mimeEmail, int desiredPriorityLevel);

    /**
     * Schedules the sending of an email message.
     *
     * @param mimeEmail            an email to be sent
     * @param scheduledDateTime    the date-time at which the email should be sent
     * @param desiredPriorityLevel the desiredPriority level for the email:
     *                             the emails with scheduledTime<=now are sent according to an order depending
     *                             on their desiredPriority level
     */
    @Async
    void schedule(Email mimeEmail, OffsetDateTime scheduledDateTime, int desiredPriorityLevel);

    /**
     * Schedules the sending of an email message at time <strong>now</strong> (UTC).
     *
     * @param mimeEmail            an email to be sent
     * @param desiredPriorityLevel the desiredPriority level for the email:
     *                             the emails with scheduledTime<=now are sent according to an order depending
     *                             on their desiredPriority level
     * @param template             the reference to the template file
     * @param modelObject          the model object to be used for the template engine, it may be null
     * @param inlinePictures       list of pictures to be rendered inline in the template
     */
    @Async
    void schedule(Email mimeEmail, int desiredPriorityLevel,
                  String template, Map<String, Object> modelObject,
                  InlinePicture... inlinePictures) throws CannotSendEmailException;

    /**
     * Schedules the sending of an email message.
     *
     * @param mimeEmail            an email to be sent
     * @param scheduledDateTime    the date-time at which the email should be sent
     * @param desiredPriorityLevel the desiredPriority level for the email:
     *                             the emails with scheduledTime<=now are sent according to an order depending
     *                             on their desiredPriority level
     * @param template             the reference to the template file
     * @param modelObject          the model object to be used for the template engine, it may be null
     * @param inlinePictures       list of pictures to be rendered inline in the template
     */
    @Async
    void schedule(Email mimeEmail, OffsetDateTime scheduledDateTime, int desiredPriorityLevel,
                  String template, Map<String, Object> modelObject,
                  InlinePicture... inlinePictures) throws CannotSendEmailException;


    default ServiceStatus status() {
        return ServiceStatus.CLOSED;
    }

}
