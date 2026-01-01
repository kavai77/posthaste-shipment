package com.posthaste.ai.multipartfile;

import org.springframework.web.multipart.MultipartFile;

public interface MultipartFileResolver {
    String fileToText(MultipartFile file);
}
