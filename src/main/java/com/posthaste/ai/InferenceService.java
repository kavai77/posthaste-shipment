package com.posthaste.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.posthaste.ai.inferenceprovider.InferenceProvider;
import com.posthaste.firebase.PromptRepository;
import com.posthaste.model.PosthasteShipment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RequiredArgsConstructor
@Component
@Slf4j
public class InferenceService {
    public static final int NOVITA_INFERENCE_ORDER = 0;
    public static final int REPLICATE_INFERENCE_ORDER = 1;

    public static final int MAX_RETRY_COUNT_PER_PROVIDER = 2;
    public static final int PREDICTION_CACHE_TIMOUT_DAYS = 7;

    private final ObjectMapper objectMapper;
    private final PromptRepository promptRepository;
    private final List<InferenceProvider> inferenceProviders;
    private String preferredProvider;

    @PostConstruct
    public void init() {
        preferredProvider = inferenceProviders.getFirst().getName();
    }

    public PosthasteShipment predict(String input) throws Exception {
        checkArgument(isNotBlank(input));
        checkArgument(input.length() <= 10000);
        Exception lastException = null;
        var savedPrediction = promptRepository.getPrediction(input);
        if (savedPrediction.isPresent()
                && preferredProvider.equals(savedPrediction.get().getInferenceProvider())
                && Duration.between(savedPrediction.get().getGeneratedTime().toInstant(), Instant.now()).toDays() < PREDICTION_CACHE_TIMOUT_DAYS) {
            try {
                return objectMapper.readValue(savedPrediction.get().getPrediction(), PosthasteShipment.class);
            } catch (JsonProcessingException e) {
                log.error("JSON returned from DB cannot be parsed", e);
            }
        }
        for (var retried = 0; retried < inferenceProviders.size() * MAX_RETRY_COUNT_PER_PROVIDER; retried++) {
            try {
                var inferenceProvider = inferenceProviders.get(retried % inferenceProviders.size());
                var response = inferenceProvider.generateResponse(input);
                PosthasteShipment predictionResponse = objectMapper.readValue(response, PosthasteShipment.class);
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
