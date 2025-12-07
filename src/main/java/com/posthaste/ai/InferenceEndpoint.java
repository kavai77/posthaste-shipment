package com.posthaste.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InferenceEndpoint {
    private final InferenceService inferenceService;

    @PostMapping(value = "/inference", consumes = "text/plain")
    public PredictionResponse quotes(@RequestBody String prompt) {
        return inferenceService.predict(prompt);
    }
}
