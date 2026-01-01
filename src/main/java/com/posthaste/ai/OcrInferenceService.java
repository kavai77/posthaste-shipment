package com.posthaste.ai;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.common.content.ContentPart;
import io.github.sashirestela.openai.domain.chat.ChatMessage;
import io.github.sashirestela.openai.domain.chat.ChatRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OcrInferenceService {

    @Value("${novita.api.key}")
    private final String novitaApiKey;

    public String generateResponse(byte[] imageBytes, String contentType) {
        var openAI = SimpleOpenAI.builder()
                .apiKey(novitaApiKey)
                .baseUrl("https://api.novita.ai/openai")
                .build();

        var chatRequest = ChatRequest.builder()
                .model("deepseek/deepseek-ocr")
                .message(ChatMessage.UserMessage.of(List.of(
                        ContentPart.ContentPartText.of(
                                "Free OCR."),
                        ContentPart.ContentPartImageUrl.of(ContentPart.ContentPartImageUrl.ImageUrl.of(
                                encode(imageBytes, contentType))))))
                .build();
        var futureChat = openAI.chatCompletions().create(chatRequest);
        return futureChat.join().firstContent();
    }

    private String encode(byte[] imageBytes, String contentType) {
        String base64String = Base64.getEncoder().encodeToString(imageBytes);
        return "data:" + contentType + ";base64," + base64String;
    }

}
