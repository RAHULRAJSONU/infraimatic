package com.ezyinfra.product.templates.service.impl;

import com.ezyinfra.product.common.dto.EntryDto;
import com.ezyinfra.product.common.enums.EntryStatus;
import com.ezyinfra.product.common.exception.NotFoundException;
import com.ezyinfra.product.common.exception.ValidationException;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
    public EntryDto createEntry(String type,
                                JsonNode normalized, JsonNode payload,
                                JsonNode processingMeta) {
        log.info("Creating entry with type: {}, normalized payload: {}, payload: {}, processingMeta: {}",type,normalized,payload,processingMeta);
        TemplateDefinitionEntity template = templateRepository
                .findTopByTypeOrderByVersionDesc(type)
                .orElseThrow(() -> new NotFoundException(
                        "Template not found: type=" + type));

        // Build schema directly from the JsonNode stored on the template
        JsonSchema schema = SCHEMA_FACTORY.getSchema(template.getJsonSchema());
        // Validate normalized payload
        // @ToDo fix it
//        Set<ValidationMessage> violations = schema.validate(normalized);
//        if (!violations.isEmpty()) {
//            Map<String, Object> details = new LinkedHashMap<>();
//            details.put("errors", violations.stream()
//                    .map(vm -> Map.of(
//                            "path", vm.getEvaluationPath(),
//                            "message", vm.getMessage()))
//                    .collect(Collectors.toList()));
//            log.error("Normalized payload failed schema validation, details: {}",details);
//            throw new ValidationException("Normalized payload failed schema validation", details);
//        }

        // Persist record
        RecordEntity record = new RecordEntity();
        record.setType(type);
        record.setTemplate(template);
        record.setTemplateVersion(template.getVersion());
        record.setStatus(EntryStatus.SUCCESS);
        record.setNormalized(normalized);
        record.setPayload(payload);
        record.setMetadata(processingMeta);

        recordRepository.save(record);

        return toDto(record);
    }

    @Override
    public EntryDto getEntry(String type, Integer version, UUID id) {
        RecordEntity record = recordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Submission not found: id=" + id));
        if (!Objects.equals(type, record.getType()) ||
                !Objects.equals(version, record.getTemplateVersion())) {
            throw new NotFoundException("Submission does not match type/version");
        }
        return toDto(record);
    }

    @Override
    public EntryDto getEntry(String type, UUID id) {
        RecordEntity record = recordRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Entry not found: id=" + id));
        return toDto(record);
    }

    @Override
    public List<EntryDto> listEntries(String type, Integer version) {
        return recordRepository.findByTypeAndTemplateVersion(type, version)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<EntryDto> listEntriesPageable(String type, Integer version, Pageable pageable) {
        Page<EntryDto> page = recordRepository.findByTypeAndTemplateVersion(type, version, pageable)
                .map(this::toDto);
        return page;
    }

    @Override
    public Page<EntryDto> listEntriesPageable(String type, Pageable pageable) {
        Page<EntryDto> page = recordRepository.findByType(type, pageable)
                .map(this::toDto);
        return page;
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
