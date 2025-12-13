package com.posthaste.ai;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class InferenceEndpoint {
    private final InferenceService inferenceService;

    @PostMapping(value = "/inference")
    @SneakyThrows
    public PredictionResponse infer(@RequestBody PredictionRequest request) {
        return inferenceService.predict(request.prompt());
    }

    @PostMapping(value = "/inference-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SneakyThrows
    public PredictionResponse documentUpload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty() || !MediaType.APPLICATION_PDF_VALUE.equals(file.getContentType())) {
            throw new IllegalArgumentException("File is empty or not a pdf file");
        }
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            return inferenceService.predict(text);
        }
    }
}
