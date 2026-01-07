package com.ezyinfra.product.infraimatic.data.dto;

import java.util.List;

public record RuntimeApproverRequest(
        int levelOrder,
        List<String> users // userIds
) {}
