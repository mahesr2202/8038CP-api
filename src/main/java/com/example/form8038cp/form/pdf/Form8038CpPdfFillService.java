package com.example.form8038cp.form.pdf;

import java.io.IOException;
import java.util.List;

public interface Form8038CpPdfFillService {
    byte[] fill(byte[] templateBytes, List<PdfFieldValue> values) throws IOException;
}
