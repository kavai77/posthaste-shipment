package com.posthaste.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class SystemPromptConfiguration {
    @Bean
    public String systemPrompt() throws IOException {
        var prePrompt = new String(getClass().getResourceAsStream("/llm-system-prompt.txt").readAllBytes());
        var schema = new String(getClass().getResourceAsStream("/shipment-schema-llm.json").readAllBytes());
        return prePrompt + "\n" + schema + "\n";
    }
}
