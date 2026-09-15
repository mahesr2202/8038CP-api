package com.example.form8038cp.form.controller;

import com.example.form8038cp.form.pdf.Form8038CpPdfService;
import com.example.form8038cp.security.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/forms")
@RequiredArgsConstructor
@Tag(name = "PDF", description = "Generate filled IRS Form 8038-CP PDF documents")
public class Form8038CpPdfController {

    private final Form8038CpPdfService pdfService;

    @Operation(summary = "Download filled Form 8038-CP PDF")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> downloadMainPdf(@PathVariable Long id) throws IOException {
        byte[] pdf = pdfService.generateMainPdf(SecurityUtil.currentUserId(), id);
        return pdfResponse(pdf, "Form8038CP_" + id + ".pdf");
    }

    @Operation(summary = "Download filled Schedule A PDF (requires Schedule A rows)")
    @GetMapping("/{id}/schedule-a-pdf")
    public ResponseEntity<byte[]> downloadScheduleAPdf(@PathVariable Long id) throws IOException {
        byte[] pdf = pdfService.generateScheduleAPdf(SecurityUtil.currentUserId(), id);
        return pdfResponse(pdf, "ScheduleA_8038CP_" + id + ".pdf");
    }

    private static ResponseEntity<byte[]> pdfResponse(byte[] pdf, String filename) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdf.length);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
