package ai.aletheia.llm.impl;

import ai.aletheia.llm.LLMClient;
import ai.aletheia.llm.LLMException;
import ai.aletheia.llm.LLMResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * OpenAI Chat Completions API client.
 *
 * <p>Requires OPENAI_API_KEY in environment. Model from OPENAI_MODEL (default: gpt-4).
 * Uses Java HttpClient; no external SDK.
 */
@Service
@ConditionalOnExpression("'${ai.aletheia.llm.openai.api-key:}'.length() > 0")
public class OpenAiLLMClient implements LLMClient {

    private static final Logger log = LoggerFactory.getLogger(OpenAiLLMClient.class);
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private static final Duration TIMEOUT = Duration.ofSeconds(60);

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public OpenAiLLMClient(
            @Value("${ai.aletheia.llm.openai.api-key:}") String apiKey,
            @Value("${ai.aletheia.llm.openai.model:gpt-4}") String model) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OPENAI_API_KEY must be set for OpenAiLLMClient");
        }
        this.apiKey = apiKey;
        this.model = model.isBlank() ? "gpt-4" : model;
        this.httpClient = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
        this.objectMapper = new ObjectMapper();
        log.info("OpenAI LLM client initialized, model={}", this.model);
    }

    @Override
    public LLMResult complete(String prompt) {
        if (prompt == null) {
            prompt = "";
        }
        try {
            String requestBody = buildRequestBody(prompt);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .timeout(TIMEOUT)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return parseResponse(response, prompt);
        } catch (LLMException e) {
            throw e;
        } catch (Exception e) {
            throw new LLMException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }

    private String buildRequestBody(String prompt) throws Exception {
        ObjectNode root = objectMapper.createObjectNode();
        root.put("model", model);
        root.put("max_tokens", 2000);
        ArrayNode messages = root.putArray("messages");
        ObjectNode msg = messages.addObject();
        msg.put("role", "user");
        msg.put("content", prompt);
        return objectMapper.writeValueAsString(root);
    }

    private LLMResult parseResponse(HttpResponse<String> response, String prompt) throws Exception {
        int status = response.statusCode();
        String body = response.body();

        if (status == 401) {
            throw new LLMException("OpenAI API key invalid or expired (401)");
        }
        if (status == 429) {
            throw new LLMException("OpenAI rate limit exceeded (429)");
        }
        if (status == 503) {
            throw new LLMException("OpenAI service unavailable (503)");
        }
        if (status < 200 || status >= 300) {
            throw new LLMException("OpenAI API error: " + status + " " + body);
        }

        JsonNode root = objectMapper.readTree(body);
        JsonNode choices = root.get("choices");
        if (choices == null || !choices.isArray() || choices.isEmpty()) {
            throw new LLMException("OpenAI response has no choices: " + body);
        }
        JsonNode first = choices.get(0);
        JsonNode message = first != null ? first.get("message") : null;
        JsonNode content = message != null ? message.get("content") : null;
        String responseText = content != null && content.isTextual() ? content.asText() : "";

        String modelId = root.has("model") && root.get("model").isTextual()
                ? root.get("model").asText()
                : model;

        log.debug("LLM complete: promptLen={}, responseLen={}, model={}", prompt.length(), responseText.length(), modelId);
        return new LLMResult(responseText, modelId);
    }
}
