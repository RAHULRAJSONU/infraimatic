package com.ezyinfra.product.infraimatic.web;

import com.ezyinfra.product.infraimatic.data.dto.ActApprovalRequest;
import com.ezyinfra.product.infraimatic.data.dto.ApprovalActionResponseDto;
import com.ezyinfra.product.infraimatic.data.dto.ApprovalTriggerRequest;
import com.ezyinfra.product.infraimatic.data.dto.AttachApprovalRequest;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalInstance;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalTask;
import com.ezyinfra.product.infraimatic.service.ApprovalActionService;
import com.ezyinfra.product.infraimatic.service.ApprovalAttachmentService;
import com.ezyinfra.product.infraimatic.service.ApprovalOrchestratorService;
import com.ezyinfra.product.infraimatic.service.ApprovalQueryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    private final ApprovalAttachmentService attachmentService;
    private final ApprovalOrchestratorService orchestrator;
    private final ApprovalActionService actionService;
    private final ApprovalQueryService queryService;

    public ApprovalController(
            ApprovalAttachmentService attachmentService,
            ApprovalOrchestratorService orchestrator,
            ApprovalActionService actionService, ApprovalQueryService queryService) {
        this.attachmentService = attachmentService;
        this.orchestrator = orchestrator;
        this.actionService = actionService;
        this.queryService = queryService;
    }

    @PostMapping("/attach")
    public void attach(@RequestBody AttachApprovalRequest req) {
        attachmentService.attach(req.entityType(), req.entityId(), req.templateId());
    }

    @PostMapping("/detach")
    public void detach(@RequestBody AttachApprovalRequest req) {
        attachmentService.detach(req.entityType(), req.entityId());
    }

    @PostMapping("/trigger/{type}/{id}")
    public void trigger(@PathVariable String type, @PathVariable String id) {
        orchestrator.trigger(type, id);
    }

    @PostMapping("/{instanceId}/act")
    public ResponseEntity<ApprovalActionResponseDto> act(
            @PathVariable UUID instanceId,
            @RequestBody ActApprovalRequest req,
            Principal principal) {

        return ResponseEntity.ok(actionService.act(
                instanceId,
                principal.getName(),
                req.roles(),
                req.approve(),
                req.comment()));
    }

    @GetMapping("/pending")
    public List<ApprovalTask> pendingApprovals(
            @RequestParam Set<String> roles,
            Principal principal) {

        return queryService.myPendingApprovals(
                principal.getName(), roles);
    }

    @GetMapping("/my-pending")
    public List<ApprovalTask> myPendingApprovals(@RequestParam("userId") String userId) {
        return queryService.myPendingApprovals(userId);
    }

    @GetMapping("/history")
    public List<ApprovalTask> myHistory(Principal principal) {
        return queryService.myApprovalHistory(principal.getName());
    }

    @GetMapping("/status/{type}/{id}")
    public ResponseEntity<?> status(
            @PathVariable String type,
            @PathVariable String id) {

        return queryService.status(type, id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/trigger/runtime")
    public ApprovalInstance triggerWithRuntime(
            @RequestBody ApprovalTriggerRequest req) {
        return orchestrator.triggerWithRuntimeApprovers(req);
    }

}
