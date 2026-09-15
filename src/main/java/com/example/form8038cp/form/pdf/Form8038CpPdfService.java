package com.example.form8038cp.form.pdf;

import java.io.IOException;

public interface Form8038CpPdfService {
    byte[] generateMainPdf(Long userId, Long submissionId) throws IOException;
    byte[] generateScheduleAPdf(Long userId, Long submissionId) throws IOException;
}
