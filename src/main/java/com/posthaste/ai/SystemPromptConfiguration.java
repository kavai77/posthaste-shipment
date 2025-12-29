package com.posthaste.ai;

import com.github.victools.jsonschema.generator.SchemaGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class SystemPromptConfiguration {
    @Bean
    public String systemPrompt(SchemaGenerator schemaGenerator) throws IOException {
        var systemPrompt = new String(getClass().getResourceAsStream("/llm-system-prompt.txt").readAllBytes());
        var schema = schemaGenerator.generateSchema(PredictionResponse.class).toPrettyString();
        return systemPrompt.replace("{{json_schema}}", schema);
    }
}
