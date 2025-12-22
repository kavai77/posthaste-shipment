package com.posthaste.ai;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@RestController
@RequiredArgsConstructor
public class InferenceEndpoint {
    private final InferenceService inferenceService;

    @GetMapping(value = "/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.of(Optional.of("OK"));
    }

    @PostMapping(value = "/inference", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SneakyThrows
    public PredictionResponse inference(
            @RequestParam(value = "prompt", required = false) String prompt,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        String input = Stream.of(prompt, stripPdf(file))
                .filter(Objects::nonNull)
                .filter(not(String::isBlank))
                .collect(Collectors.joining("\n"));
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Empty input");
        }
        return inferenceService.predict(input);
    }

    private String stripPdf(MultipartFile file) {
        if (file == null || file.isEmpty() || !MediaType.APPLICATION_PDF_VALUE.equals(file.getContentType())) {
            return null;
        }
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        } catch (Exception e) {
            return null;
        }
    }
}
