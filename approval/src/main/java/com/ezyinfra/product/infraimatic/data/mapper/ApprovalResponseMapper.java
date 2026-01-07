package com.ezyinfra.product.infraimatic.data.mapper;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalActionResponseDto;
import com.ezyinfra.product.infraimatic.data.dto.ApprovalTaskTimelineDto;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalInstance;
import com.ezyinfra.product.infraimatic.data.entity.ApprovalTask;

import java.util.Comparator;
import java.util.List;

public class ApprovalResponseMapper {

    public static ApprovalActionResponseDto toResponse(ApprovalInstance instance) {

        List<ApprovalTaskTimelineDto> timeline =
                instance.getTasks().stream()
                        .sorted(Comparator
                                .comparingInt(ApprovalTask::getLevelOrder)
                                .thenComparingInt(ApprovalTask::getApprovalGroup))
                        .map(t -> new ApprovalTaskTimelineDto(
                                t.getLevelOrder(),
                                t.getApprovalGroup(),
                                t.getApproverType(),
                                t.getApprover(),
                                t.getStatus(),
                                t.getActedAt(),
                                t.getActedBy(),
                                t.getComment()
                        ))
                        .toList();

        return new ApprovalActionResponseDto(
                instance.getId(),
                instance.getEntityType(),
                instance.getEntityId(),
                instance.getStatus(),
                instance.isLocked(),
                timeline
        );
    }
}
