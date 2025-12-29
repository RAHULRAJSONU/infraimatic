package com.ezyinfra.product.templates.service.impl;

import com.ezyinfra.product.common.dto.TemplateDto;
import com.ezyinfra.product.common.exception.NotFoundException;
import com.ezyinfra.product.infra.entity.TemplateDefinitionEntity;
import com.ezyinfra.product.infra.repository.TemplateDefinitionRepository;
import com.ezyinfra.product.templates.service.TemplateService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the {@link TemplateService}. This service interacts with
 * the persistence layer to create, retrieve and list templates on a per
 * tenant basis.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class TemplateServiceImpl implements TemplateService {

    private final TemplateDefinitionRepository templateRepository;

    @Override
    public TemplateDto createTemplate(String type, String name, JsonNode jsonSchema) {
        // Determine next version
        int nextVersion = 1;
        List<TemplateDefinitionEntity> existing = templateRepository.findByTypeOrderByVersionDesc(type);
        if (!existing.isEmpty()) {
            nextVersion = existing.get(0).getVersion() + 1;
        }
        TemplateDefinitionEntity entity = new TemplateDefinitionEntity();
        entity.setType(type);
        entity.setVersion(nextVersion);
        entity.setName(name);
        entity.setJsonSchema(jsonSchema);
        templateRepository.save(entity);
        return toDto(entity);
    }

    @Override
    public TemplateDto getTemplate(String type, Integer version) {
        TemplateDefinitionEntity entity = templateRepository
                .findByTypeAndVersion(type, version)
                .orElseThrow(() -> new NotFoundException("Template not found: type=" + type + ", version=" + version));
        return toDto(entity);
    }

    @Override
    public TemplateDto getLatestTemplate(String type) {
        Optional<TemplateDefinitionEntity> latestList = templateRepository.findTopByTypeOrderByVersionDesc(type);
        if (latestList.isEmpty()) {
            throw new NotFoundException("Template not found: type=" + type);
        }
        return toDto(latestList.get());
    }

    @Override
    public List<TemplateDto> listTemplates(String type) {
        return templateRepository.findByTypeOrderByVersionDesc(type)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private TemplateDto toDto(TemplateDefinitionEntity entity) {
        return new TemplateDto(
                entity.getId(),
                entity.getType(),
                entity.getVersion(),
                entity.getName(),
                entity.getJsonSchema()
        );
    }
}