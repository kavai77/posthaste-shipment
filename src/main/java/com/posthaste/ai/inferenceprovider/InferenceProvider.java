package com.posthaste.ai.inferenceprovider;

public interface InferenceProvider {
    int MAX_TOKENS = 4092;

    String generateResponse(String input);

    String getName();
}
