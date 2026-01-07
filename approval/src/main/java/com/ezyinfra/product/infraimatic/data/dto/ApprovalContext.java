package com.ezyinfra.product.infraimatic.data.dto;

import java.util.List;

public record ApprovalContext(
        List<String> approvers // userIds
) {}
