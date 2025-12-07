package com.posthaste.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class InferenceEndpoint {
    private final InferenceService inferenceService;

    @PostMapping(value = "/inference")
    public PredictionResponse quotes(@RequestBody PredictionRequest request) {
        return inferenceService.predict(request.prompt());
    }
}
