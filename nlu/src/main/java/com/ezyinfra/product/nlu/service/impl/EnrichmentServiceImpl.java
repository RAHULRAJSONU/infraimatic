package com.ezyinfra.product.nlu.service.impl;

import com.ezyinfra.product.common.dto.NluParseResponse;
import com.ezyinfra.product.common.dto.NluSubmitResponse;
import com.ezyinfra.product.common.dto.EntryDto;
import com.ezyinfra.product.common.dto.TemplateDto;
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

/**
 * Basic implementation of the {@link EnrichmentService} that extracts
 * structured information from movement register phrases. It uses a simple
 * regular expression to parse equipment id, from and to site and an ISO date
 * string. This implementation is sufficient for demonstration and testing
 * purposes but should be replaced by a more robust NLP model in production.
 */
@Service
@RequiredArgsConstructor
public class EnrichmentServiceImpl implements EnrichmentService {

    private static final Pattern MOVEMENT_PATTERN = Pattern.compile(
            "(?i)(?<equipment>[A-Za-z0-9-]+)\\s+moved\\s+from\\s+(?<from>[A-Za-z0-9-]+)\\s+to\\s+(?<to>[A-Za-z0-9-]+).*?at\\s+(?<date>[0-9T:\\-]+Z?)");

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
        Matcher matcher = MOVEMENT_PATTERN.matcher(text);
        if (matcher.find()) {
            normalized.put("equipmentId", matcher.group("equipment"));
            normalized.put("fromSite", matcher.group("from"));
            normalized.put("toSite", matcher.group("to"));
            String dateStr = matcher.group("date");
            // Normalise date: if missing seconds, append ":00Z" or ensure ISO format
            String iso;
            if (dateStr.length() == 16) { // e.g. 2025-09-28T10:20
                iso = dateStr + ":00Z";
            } else if (!dateStr.endsWith("Z")) {
                iso = dateStr + "Z";
            } else {
                iso = dateStr;
            }
            // Validate format
            try {
                OffsetDateTime.parse(iso, DateTimeFormatter.ISO_DATE_TIME);
                normalized.put("date", iso);
            } catch (Exception e) {
                // fallback: store raw string
                normalized.put("date", iso);
            }
            // Additional fields can be extracted (e.g. movedBy, reason) by more complex NLP
        } else {
            // If pattern doesn't match, return empty normalized with low confidence
            confidence = 0.0;
        }
        return new NluParseResponse(templateType, version, normalized, confidence);
    }

    @Override
    public NluSubmitResponse submit(String tenantId, String text) {
        NluParseResponse parseResponse = parse(tenantId, text);
        EntryDto submission = entryService.createEntry(
                tenantId,
                parseResponse.templateType(),
                parseResponse.templateVersion(),
                parseResponse.normalized(),
                null,
                null);
        return new NluSubmitResponse(submission.id(), parseResponse.normalized());
    }
}