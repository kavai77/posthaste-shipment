package com.posthaste.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Component
@RequiredArgsConstructor
public class InferenceService {
    public static final String MODEL = "https://api.replicate.com/v1/models/meta/meta-llama-3-8b-instruct/predictions";

    public static final String DATA_PREFIX = "data: ";
    public static final int MAX_TOKENS = 4092;

    @Value("${replicate.bearer.auth}")
    private final String replicateBearerAuth;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;

    @SneakyThrows
    public PredictionResponse predict(String input) {
        checkArgument(isNotBlank(input));
        checkArgument(input.length() <= 1000);

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(replicateBearerAuth);
        PredictApiRequest.builder().build();

        HttpEntity<PredictApiRequest> entity = new HttpEntity<>(PredictApiRequest.builder()
                .input(PredictApiInputRequest.builder()
                        .prompt(systemPrompt + input)
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
                                if (data.isEmpty()) {
                                    if (!sb.isEmpty()) {
                                        sb.append("\n");
                                    }
                                } else {
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
        return objectMapper.readValue(content.substring(firstBracket, lastBracket + 1), PredictionResponse.class);
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
