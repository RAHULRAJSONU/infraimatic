package com.ezyinfra.product.infraimatic.web;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalTimelineEventDto;
import com.ezyinfra.product.infraimatic.service.ApprovalTimelineService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalTimelineController {

    private final ApprovalTimelineService timelineService;

    public ApprovalTimelineController(
            ApprovalTimelineService timelineService) {
        this.timelineService = timelineService;
    }

    @GetMapping("/{instanceId}/timeline")
    public List<ApprovalTimelineEventDto> timeline(
            @PathVariable UUID instanceId) {
        return timelineService.timeline(instanceId);
    }
}
