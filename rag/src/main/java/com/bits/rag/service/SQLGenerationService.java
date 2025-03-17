package com.bits.rag.service;

import com.bits.rag.dto.ColumnResult;
import com.bits.rag.dto.TableResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SQLGenerationService {

    @Value("${huggingface.api-key}")
    private String huggingfaceApiKey;

    private final WebClient webClient;
    private final JdbcSearchService jdbcSearchService;

    public SQLGenerationService(WebClient.Builder webClientBuilder, JdbcSearchService jdbcSearchService) {
        this.webClient = webClientBuilder.baseUrl("https://router.huggingface.co/fireworks-ai/v1/chat/completions").build();
        this.jdbcSearchService = jdbcSearchService;
    }

    public String generateSql(String userQuery, List<TableResult> tables, List<ColumnResult> columns) {

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "accounts/fireworks/models/llama-v3p2-3b-instruct");

        List<Map<String, Object>> messages = new ArrayList<>();

        StringBuilder systemContent = new StringBuilder("You are a helpful assistant that generates SQL queries based on user questions from the relevant tables and columns provide.\n");

        if (tables != null && !tables.isEmpty()) {
            systemContent.append("Here are the relevant tables:\n");
            systemContent.append(tables.stream()
                    .map(table -> "- " + table.getTableName() + ": " + table.getDescription())
                    .collect(Collectors.joining("\n")));
            systemContent.append("\n");
        }

        if (columns != null && !columns.isEmpty()) {
            // Create a map of table IDs to table names
            Map<UUID, String> tableIdToName = tables.stream()
                    .collect(Collectors.toMap(TableResult::getTableId, TableResult::getTableName));

            systemContent.append("Here are the relevant columns:\n");
            systemContent.append(columns.stream()
                    .map(column -> {
                        String tableName = tableIdToName.get(column.getTableId());
                        return "- " + tableName + "." + column.getColumnName() + ": " + column.getDescription();
                    })
                    .collect(Collectors.joining("\n")));
            systemContent.append("\n");
        }

        systemContent.append("Use this information to generate the most accurate SQL query with appropriate joins and where conditions if applicable. Respond with ONLY the SQL query. Do NOT include any other text or hallucinate. If you are unable to create a query or seem the data is not sufficient, respond with 'Unable to generate query'.\n");
        systemContent.append("Preferably, provide the response in a structured JSON format, with the SQL query under the key 'sql'. For example: {\"sql\":\"SELECT ...\"}");

        Map<String, Object> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", systemContent.toString());
        messages.add(systemMessage);

        Map<String, Object> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", userQuery);
        messages.add(userMessage);

        requestBody.put("messages", messages);
        requestBody.put("max_tokens", 5000);

        return webClient.post()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + huggingfaceApiKey)
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(responseBody -> {
                    if (responseBody != null && responseBody.containsKey("choices")) {
                        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                        if (!choices.isEmpty() && choices.get(0).containsKey("message")) {
                            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                            String generatedText = (String) message.get("content");
                            return Mono.just(generatedText);
                        }
                    }
                    return Mono.just("Unable to generate SQL.");
                })
                .block();
    }
}