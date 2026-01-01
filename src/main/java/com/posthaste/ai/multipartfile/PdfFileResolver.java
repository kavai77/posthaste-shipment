package com.posthaste.ai.multipartfile;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class PdfFileResolver implements MultipartFileResolver {
    @Override
    public String fileToText(MultipartFile file) {
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
