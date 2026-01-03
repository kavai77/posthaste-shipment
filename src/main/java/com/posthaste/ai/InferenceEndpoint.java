package com.posthaste.ai;

import com.posthaste.ai.multipartfile.MultipartFileResolver;
import com.posthaste.model.PosthasteShipment;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;

@RestController
@RequiredArgsConstructor
public class InferenceEndpoint {
    private final InferenceService inferenceService;
    private final List<MultipartFileResolver> multipartFileResolverList;

    @GetMapping(value = "/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.of(Optional.of("OK"));
    }

    @PostMapping(value = "/inference", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @SneakyThrows
    public PosthasteShipment inference(
            @RequestParam(value = "prompt", required = false) String prompt,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        String input = Stream.concat(
                        Stream.of(prompt),
                        multipartFileResolverList.stream().map(resolver -> resolver.fileToText(file)))
                .filter(Objects::nonNull)
                .filter(not(String::isBlank))
                .collect(Collectors.joining("\n"));

        return inferenceService.predict(input);
    }
}
