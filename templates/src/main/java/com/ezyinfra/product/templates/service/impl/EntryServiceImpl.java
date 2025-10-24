package com.ezyinfra.product.templates.service.impl;

import com.ezyinfra.product.common.dto.EntryDto;
import com.ezyinfra.product.common.exception.NotFoundException;
import com.ezyinfra.product.common.exception.ValidationException;
import com.ezyinfra.product.domain.EntryStatus;
import com.ezyinfra.product.infra.entity.RecordEntity;
import com.ezyinfra.product.infra.entity.TemplateDefinitionEntity;
import com.ezyinfra.product.infra.repository.RecordRepository;
import com.ezyinfra.product.infra.repository.TemplateDefinitionRepository;
import com.ezyinfra.product.templates.service.EntryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EntryServiceImpl implements EntryService {

    private static final JsonSchemaFactory SCHEMA_FACTORY =
            JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    private final TemplateDefinitionRepository templateRepository;
    private final RecordRepository recordRepository;
    private final ObjectMapper mapper; // injected by Spring

    @Override
    public EntryDto createEntry(String tenantId, String type, Integer version,
                                JsonNode normalized, JsonNode payload,
                                JsonNode processingMeta) {

        TemplateDefinitionEntity template = templateRepository
                .findByTenantIdAndTypeAndVersion(tenantId, type, version)
                .orElseThrow(() -> new NotFoundException(
                        "Template not found: tenant=" + tenantId + ", type=" + type + ", version=" + version));

        // Build schema directly from the JsonNode stored on the template
        JsonSchema schema = SCHEMA_FACTORY.getSchema(template.getJsonSchema());

        // Validate normalized payload
        Set<ValidationMessage> violations = schema.validate(normalized);
        if (!violations.isEmpty()) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("errors", violations.stream()
                    .map(vm -> Map.of(
                            "path", vm.getEvaluationPath(),
                            "message", vm.getMessage()))
                    .collect(Collectors.toList()));
            throw new ValidationException("Normalized payload failed schema validation", details);
        }

        // Persist record
        RecordEntity record = new RecordEntity();
        record.setTenantId(tenantId);
        record.setType(type);
        record.setTemplate(template);
        record.setTemplateVersion(version);
        record.setStatus(EntryStatus.SUCCESS);
        record.setNormalized(normalized);
        record.setPayload(payload);
        record.setMetadata(processingMeta);

        recordRepository.save(record);

        return toDto(record);
    }

    @Override
    public EntryDto getEntry(String tenantId, String type, Integer version, Long id) {
        RecordEntity record = recordRepository.findByIdAndTenantId(id, tenantId)
                .orElseThrow(() -> new NotFoundException("Submission not found: id=" + id + ", tenant=" + tenantId));
        if (!Objects.equals(type, record.getType()) ||
                !Objects.equals(version, record.getTemplateVersion())) {
            throw new NotFoundException("Submission does not match type/version");
        }
        return toDto(record);
    }

    @Override
    public List<EntryDto> listEntries(String tenantId, String type, Integer version) {
        return recordRepository.findByTenantIdAndTypeAndTemplateVersion(tenantId, type, version)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private EntryDto toDto(RecordEntity record) {
        return new EntryDto(
                record.getId(),
                record.getTenantId(),
                record.getType(),
                record.getTemplateVersion(),
                record.getNormalized()
        );
    }
}
