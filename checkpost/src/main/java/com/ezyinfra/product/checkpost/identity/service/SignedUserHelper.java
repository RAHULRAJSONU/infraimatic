package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.common.utility.AppConstant;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SignedUserHelper {

    /**
     * get user id of signed user from spring security context
     *
     * @return user id of signed user
     */
    public static User user() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        try {
            return ((User) auth.getPrincipal());
        } catch (ClassCastException e) {
            var testUser = new User();
            testUser.setUsername(AppConstant.Security.ANONYMOUS_USER);
            return testUser;
        }
    }
}