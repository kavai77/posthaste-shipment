package com.posthaste.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posthaste.firebase.PromptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RequiredArgsConstructor
@Component
@Slf4j
public class InferenceService {
    public static final int BASETEN_INFERENCE_ORDER = 0;
    public static final int REPLICATE_INFERENCE_ORDER = 1;

    public static final int MAX_RETRY_COUNT_PER_PROVIDER = 2;

    private final ObjectMapper objectMapper;
    private final PromptRepository promptRepository;
    private final List<InferenceProvider> inferenceProviders;

    public PredictionResponse predict(String input) throws Exception {
        checkArgument(isNotBlank(input));
        checkArgument(input.length() <= 10000);
        Exception lastException = null;
        var savedPrediction = promptRepository.getPrediction(input);
        if (savedPrediction.isPresent()) {
            try {
                return objectMapper.readValue(savedPrediction.get(), PredictionResponse.class);
            } catch (JsonProcessingException e) {
                log.error("JSON returned from DB cannot be parsed", e);
            }
        }
        for (var retried = 0; retried < inferenceProviders.size() * MAX_RETRY_COUNT_PER_PROVIDER; retried++) {
            try {
                var inferenceProvider = inferenceProviders.get(retried % inferenceProviders.size());
                var response = inferenceProvider.generateResponse(input);
                PredictionResponse predictionResponse = objectMapper.readValue(response, PredictionResponse.class);
                promptRepository.savePrompt(PromptRepository.Prompt.builder()
                        .prompt(input)
                        .prediction(objectMapper.writeValueAsString(predictionResponse))
                        .retryCount(retried)
                        .generatedTime(new Date())
                        .lastAccessTime(new Date())
                        .accessCount(0)
                        .inferenceProvider(inferenceProvider.getName())
                        .build());
                return predictionResponse;
            } catch (RuntimeException | JsonProcessingException e) {
                log.warn("JSON parsing error. Retrying...", e);
                lastException = e;
            }
        }
        log.error("JSON parsing error. Not retrying anymore.", lastException);
        throw lastException;
    }
}
