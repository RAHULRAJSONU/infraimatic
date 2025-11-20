package com.ezyinfra.product.nlu.service.impl;

import com.ezyinfra.product.common.dto.NluParseResponse;
import com.ezyinfra.product.common.dto.NluSubmitResponse;
import com.ezyinfra.product.common.dto.EntryDto;
import com.ezyinfra.product.common.dto.TemplateDto;
import com.ezyinfra.product.common.exception.NotFoundException;
import com.ezyinfra.product.nlu.service.EnrichmentService;
import com.ezyinfra.product.templates.service.EntryService;
import com.ezyinfra.product.templates.service.TemplateService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class EnrichmentServiceImpl implements EnrichmentService {

    private final TemplateService templateService;
    private final EntryService entryService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public NluParseResponse parse(String tenantId, String text) {
        // For demonstration we assume the target template type is fixed. In a real
        // system this might be predicted by a classifier.
        String templateType = "tt_movement_register";
        TemplateDto template = templateService.getLatestTemplate(tenantId, templateType);
        int version = template.version();

        ObjectNode normalized = objectMapper.createObjectNode();
        double confidence = 1.0;

        return new NluParseResponse(templateType, version, normalized, confidence);
    }

    @Override
    public NluParseResponse parse(String tenantId, String type, String text) {
        TemplateDto template = templateService.getLatestTemplate(tenantId, type);

        if(template == null)throw new NotFoundException(String.format(type+" type not configured, contact your administration."));

        return null;
    }

    @Override
    public NluSubmitResponse submit(String tenantId, String text) {
        NluParseResponse parseResponse = parse(tenantId, text);
        EntryDto submission = entryService.createEntry(
                tenantId,
                parseResponse.templateType(),
                parseResponse.normalized(),
                null,
                null);
        return new NluSubmitResponse(submission.id(), parseResponse.normalized());
    }
}