package com.ezyinfra.product.notification.email.logging;


import com.ezyinfra.product.notification.email.model.Email;

public interface EmailRenderer {

    String render(Email email);

}
