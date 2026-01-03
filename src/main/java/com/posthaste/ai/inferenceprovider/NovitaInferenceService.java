package com.posthaste.ai.inferenceprovider;

import com.posthaste.model.PosthasteShipment;
import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.ResponseFormat;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static com.posthaste.ai.InferenceService.NOVITA_INFERENCE_ORDER;

@Component
@RequiredArgsConstructor
@Order(NOVITA_INFERENCE_ORDER)
public class NovitaInferenceService implements InferenceProvider {

    @Value("${novita.api.key}")
    private final String novitaApiKey;

    private ResponseFormat responseFormat;
    private String systemMessage;

    @SneakyThrows
    @PostConstruct
    public void init() {
        this.responseFormat = ResponseFormat.jsonSchema(ResponseFormat.JsonSchema.builder()
                .name("PredictionResponse")
                .schemaClass(PosthasteShipment.class)
                .build());
        try (var systemPromptStream = getClass().getResourceAsStream("/system-prompt.txt")) {
            this.systemMessage = new String(systemPromptStream.readAllBytes());
        }
    }

    @Override
    public String generateResponse(String input) {
        var openAI = SimpleOpenAI.builder()
                .apiKey(novitaApiKey)
                .baseUrl("https://api.novita.ai/openai")
                .build();

        var chatRequest = ChatRequest.builder()
                .model("deepseek/deepseek-v3.2-exp")
                .message(ChatMessage.SystemMessage.of(systemMessage))
                .message(ChatMessage.UserMessage.of(input))
                .responseFormat(responseFormat)
                .temperature(0.0)
                .maxCompletionTokens(MAX_TOKENS)
                .build();
        var futureChat = openAI.chatCompletions().create(chatRequest);
        return futureChat.join().firstContent();
    }

    @Override
    public String getName() {
        return "Novita/DeepSeek-V3.2";
    }
}
