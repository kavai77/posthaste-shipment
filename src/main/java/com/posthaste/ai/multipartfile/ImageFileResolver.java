package com.posthaste.ai.multipartfile;

import com.posthaste.ai.OcrInferenceService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class ImageFileResolver implements MultipartFileResolver {
    private final OcrInferenceService ocrInferenceService;

    @Override
    public String fileToText(MultipartFile file) {
        if (file == null || file.isEmpty() || !StringUtils.startsWith(file.getContentType(), "image")) {
            return null;
        }
        try {
            String response = ocrInferenceService.generateResponse(file.getBytes(), file.getContentType());
            return response;
        } catch (IOException e) {
            return null;
        }
    }
}
