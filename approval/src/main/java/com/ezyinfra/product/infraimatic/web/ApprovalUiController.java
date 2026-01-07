package com.ezyinfra.product.infraimatic.web;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalSummaryDto;
import com.ezyinfra.product.infraimatic.data.dto.PendingApprovalUiDto;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalInstance;
import com.ezyinfra.product.infraimatic.service.ApprovalQueryService;
import com.ezyinfra.product.infraimatic.service.ApprovalUiMapper;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/ui/approvals")
public class ApprovalUiController {

    private final ApprovalQueryService queryService;
    private final ApprovalUiMapper mapper;

    public ApprovalUiController(ApprovalQueryService queryService, ApprovalUiMapper mapper) {
        this.queryService = queryService;
        this.mapper = mapper;
    }

    @GetMapping("/pending")
    public List<PendingApprovalUiDto> pending(
            @RequestParam Set<String> roles,
            Principal principal) {

        return queryService
                .myPendingApprovals(principal.getName(), roles)
                .stream()
                .map(mapper::toPendingUi)
                .toList();
    }

    @GetMapping("/summary/{type}/{id}")
    public ApprovalSummaryDto summary(
            @PathVariable String type,
            @PathVariable String id) {

        ApprovalInstance i = queryService.status(type, id)
                .orElseThrow();

        return new ApprovalSummaryDto(
            i.getId(),
            i.getEntityType(),
            i.getEntityId(),
            i.getStatus(),
            i.isLocked(),
            i.getCreatedAt()
        );
    }
}
