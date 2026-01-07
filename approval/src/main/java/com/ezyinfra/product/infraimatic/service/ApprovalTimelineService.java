package com.ezyinfra.product.infraimatic.service;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalTimelineEventDto;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalInstance;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalTask;
import com.ezyinfra.product.infraimatic.data.repository.ApprovalInstanceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ApprovalTimelineService {

    private final ApprovalInstanceRepository instanceRepo;

    public ApprovalTimelineService(ApprovalInstanceRepository instanceRepo) {
        this.instanceRepo = instanceRepo;
    }

    public List<ApprovalTimelineEventDto> timeline(UUID instanceId) {

        ApprovalInstance instance =
                instanceRepo.findById(instanceId).orElseThrow();

        return instance.getTasks().stream()
                .filter(t -> t.getActedAt() != null)
                .sorted(Comparator.comparing(ApprovalTask::getActedAt))
                .map(t -> new ApprovalTimelineEventDto(
                        t.getLevelOrder(),
                        t.getApprovalGroup(),
                        t.getApprover(),
                        t.getApproverType(),
                        t.getStatus(),
                        t.getActedAt(),
                        t.getActedBy(),
                        t.getComment()
                ))
                .toList();
    }
}
