package com.bits.rag.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Map;

@Service
@Slf4j
public class EmbeddingService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public EmbeddingService(
            @Value("${huggingface.api-key}") String apiKey,
            @Value("${huggingface.embedding-url}") String apiUrl) {

        this.webClient = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();

        this.objectMapper = new ObjectMapper();
    }

    public float[] generateEmbedding(String text) {
        try {
            Map<String, Object> payload = Map.of(
                    "inputs", text,
                    "options", Map.of("wait_for_model", true)
            );

            String response = webClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(90));

            return parseEmbeddingResponse(response);

        } catch (WebClientResponseException e) {
            log.error("API Error [{}]: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Embedding request failed: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            throw new RuntimeException("Embedding generation failed");
        }
    }

    private float[] parseEmbeddingResponse(String jsonResponse) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(jsonResponse);

        if (root.isArray()) {
            return parseFloatArray(root);
        }else{
            throw new RuntimeException("Unexpected batch response");
        }
    }

    private float[] parseFloatArray(JsonNode node) throws JsonProcessingException {
        return objectMapper.treeToValue(node, float[].class);
    }
}