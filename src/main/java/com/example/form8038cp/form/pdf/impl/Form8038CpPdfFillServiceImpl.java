package com.example.form8038cp.form.pdf.impl;

import com.example.form8038cp.form.pdf.Form8038CpPdfFillService;
import com.example.form8038cp.form.pdf.PdfFieldValue;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDCheckBox;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class Form8038CpPdfFillServiceImpl implements Form8038CpPdfFillService {

    private static final Set<String> TRUTHY_VALUES = Set.of("true", "1", "yes", "on");

    @Override
    public byte[] fill(byte[] templateBytes, List<PdfFieldValue> values) throws IOException {
        try (PDDocument doc = Loader.loadPDF(templateBytes)) {
            PDAcroForm acroForm = doc.getDocumentCatalog().getAcroForm();
            if (acroForm == null) {
                throw new IllegalStateException("PDF template has no AcroForm — cannot fill fields.");
            }
            acroForm.setNeedAppearances(true);

            int filled = 0, skipped = 0;
            for (PdfFieldValue fv : values) {
                PDField field = acroForm.getField(fv.pdfFieldPath());
                if (field == null) {
                    skipped++;
                    log.warn("PDF field not found: {}", fv.pdfFieldPath());
                    continue;
                }
                if (field instanceof PDCheckBox checkbox) {
                    if (TRUTHY_VALUES.contains(fv.value().toLowerCase().trim())) {
                        checkbox.check();
                    } else {
                        checkbox.unCheck();
                    }
                } else {
                    field.setValue(fv.value());
                }
                filled++;
            }
            log.info("PDF fill complete: filled={}, skipped={}", filled, skipped);

            acroForm.refreshAppearances();
            acroForm.flatten();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            doc.save(out);
            return out.toByteArray();
        }
    }
}
