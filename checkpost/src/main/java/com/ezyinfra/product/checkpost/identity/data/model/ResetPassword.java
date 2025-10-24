package com.ezyinfra.product.checkpost.identity.data.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPassword {

    private String userEmail;
    private String redirectUrl;
}
