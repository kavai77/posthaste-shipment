package com.posthaste.ai;

import com.github.victools.jsonschema.generator.SchemaGenerator;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.posthaste.ai.InferenceService.REPLICATE_INFERENCE_ORDER;

@Component
@RequiredArgsConstructor
@Order(REPLICATE_INFERENCE_ORDER)
public class ReplicateInferenceProvider implements InferenceProvider {
    public static final String MODEL = "https://api.replicate.com/v1/models/deepseek-ai/deepseek-v3.1/predictions";

    public static final String DATA_PREFIX = "data: ";

    @Value("${replicate.bearer.auth}")
    private final String replicateBearerAuth;
    private final SchemaGenerator schemaGenerator;

    private String systemMessage;

    @SneakyThrows
    @PostConstruct
    public void init() {
        try (var rawSystemPromptStream = getClass().getResourceAsStream("/raw-system-prompt.txt");
             var systemPromptStream = getClass().getResourceAsStream("/system-prompt.txt")) {
            var rawSystemPrompt = new String(rawSystemPromptStream.readAllBytes());
            var systemPrompt = new String(systemPromptStream.readAllBytes());
            var schema = schemaGenerator.generateSchema(PredictionResponse.class).toPrettyString();
            this.systemMessage = rawSystemPrompt
                    .replace("{{json_schema}}", schema)
                    .replace("{{system_prompt}}", systemPrompt);
        }
    }

    @Override
    public String generateResponse(String input) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(replicateBearerAuth);

        HttpEntity<PredictApiRequest> entity = new HttpEntity<>(PredictApiRequest.builder()
                .input(PredictApiInputRequest.builder()
                        .prompt(systemMessage.replace("{{user_prompt}}", input))
                        .max_tokens(MAX_TOKENS)
                        .build())
                .build(), headers);
        var predictApiResponse = restTemplate.exchange(MODEL, HttpMethod.POST, entity, PredictApiResponse.class);
        StringBuilder sb = new StringBuilder();
        try (var httpResponse = restTemplate.execute(predictApiResponse.getBody().urls().stream(), HttpMethod.GET,
                request -> request.getHeaders().add("Accept", "text/event-stream"),
                response -> {
                    String line;
                    try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(response.getBody()))) {
                        while (!(line = bufferedReader.readLine()).equals("event: done")) {
                            if (line.startsWith(DATA_PREFIX)) {
                                String data = line.substring(DATA_PREFIX.length());
                                if (!data.isEmpty()) {
                                    sb.append(data);
                                }
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return response;
                })) {
        }
        var content = sb.toString();
        var firstBracket = content.indexOf('{');
        var lastBracket = content.lastIndexOf('}');
        return content.substring(firstBracket, lastBracket + 1);
    }

    @Override
    public String getName() {
        return "REPLICATE";
    }

    @Builder
    public record PredictApiRequest(PredictApiInputRequest input) {
    }

    @Builder(toBuilder = true)
    public record PredictApiInputRequest(String prompt, int max_tokens) {
    }

    public record PredictApiResponse(PredictApiUrlsResponse urls) {
    }

    public record PredictApiUrlsResponse(String cancel, String get, String stream) {
    }
}
