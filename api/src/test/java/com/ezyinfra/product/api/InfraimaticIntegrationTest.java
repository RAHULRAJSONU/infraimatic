package com.ezyinfra.product.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.test.LocalServerPort;
import org.springframework.boot.web.server.test.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class InfraimaticIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("infraimatic")
            .withUsername("postgres")
            .withPassword("postgres");
    private final ObjectMapper objectMapper = new ObjectMapper();
    @LocalServerPort
    int port;
    @Autowired
    TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("infraimatic.security.enabled", () -> "false");
    }

    @Test
    void testNluParseSubmitAndRetrieve() throws Exception {
        String baseUrl = "http://localhost:" + port;
        String text = "Pump-3 moved from Site-A to Site-B for maintenance at 2025-09-28T10:20:00Z";

        // Parse
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String parseBody = "{\"text\":\"" + text + "\"}";
        HttpEntity<String> parseRequest = new HttpEntity<>(parseBody, headers);
        ResponseEntity<String> parseResponse = restTemplate.postForEntity(baseUrl + "/api/v1/sample_tenant/nlu/parse", parseRequest, String.class);
        assertThat(parseResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode parseJson = objectMapper.readTree(parseResponse.getBody());
        assertThat(parseJson.get("templateType").asText()).isEqualTo("tt_movement_register");
        assertThat(parseJson.get("templateVersion").asInt()).isEqualTo(1);
        JsonNode norm = parseJson.get("normalized");
        assertThat(norm.get("equipmentId").asText()).isEqualTo("Pump-3");
        assertThat(norm.get("fromSite").asText()).isEqualTo("Site-A");
        assertThat(norm.get("toSite").asText()).isEqualTo("Site-B");
        assertThat(norm.get("date").asText()).startsWith("2025-09-28T10:20");

        // Submit
        String submitBody = "{\"text\":\"" + text + "\"}";
        HttpEntity<String> submitRequest = new HttpEntity<>(submitBody, headers);
        ResponseEntity<String> submitResponse = restTemplate.postForEntity(baseUrl + "/api/v1/sample_tenant/nlu/submit", submitRequest, String.class);
        assertThat(submitResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        JsonNode submitJson = objectMapper.readTree(submitResponse.getBody());
        long submissionId = submitJson.get("submissionId").asLong();
        JsonNode normalized = submitJson.get("normalized");
        assertThat(submissionId).isPositive();
        assertThat(normalized.get("equipmentId").asText()).isEqualTo("Pump-3");

        // Retrieve via submissions endpoint
        ResponseEntity<String> recordResponse = restTemplate.getForEntity(baseUrl + "/api/v1/sample_tenant/templates/tt_movement_register/1/submissions/" + submissionId, String.class);
        assertThat(recordResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        JsonNode recordJson = objectMapper.readTree(recordResponse.getBody());
        assertThat(recordJson.get("id").asLong()).isEqualTo(submissionId);
        JsonNode recNorm = recordJson.get("normalized");
        assertThat(recNorm.get("equipmentId").asText()).isEqualTo("Pump-3");
        assertThat(recNorm.get("fromSite").asText()).isEqualTo("Site-A");
        assertThat(recNorm.get("toSite").asText()).isEqualTo("Site-B");
    }
}