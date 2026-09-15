package com.example.form8038cp.form.pdf.impl;

import com.example.form8038cp.form.entity.FormPartII;
import com.example.form8038cp.form.entity.FormPartIII;
import com.example.form8038cp.form.entity.FormScheduleA;
import com.example.form8038cp.form.pdf.Form8038CpPdfFillService;
import com.example.form8038cp.form.pdf.PdfFieldValue;
import com.example.form8038cp.form.pdf.ScheduleAPdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ScheduleAPdfServiceImpl implements ScheduleAPdfService {

    private static final String SA   = "topmostSubform[0].";
    private static final String P1T  = SA + "Page1[0].Page1_Table[0].";
    private static final String P2T  = SA + "Page2[0].Page2_Table[0].";

    private final Form8038CpPdfFillService fillService;
    private final String templatePath;

    public ScheduleAPdfServiceImpl(
            Form8038CpPdfFillService fillService,
            @Value("${app.irs.pdf.schedule-a-template}") String templatePath) {
        this.fillService  = fillService;
        this.templatePath = templatePath;
    }

    @Override
    public byte[] generateScheduleA(
            FormPartII partII,
            FormPartIII partIII,
            List<FormScheduleA> rows) throws IOException {

        ClassPathResource resource = new ClassPathResource(templatePath);
        if (!resource.exists()) {
            throw new IllegalStateException("Schedule A PDF template not found: " + templatePath);
        }

        String bondType = (partII != null) ? partII.getLine17c() : null;
        List<PdfFieldValue> fields = new ArrayList<>();

        // Header fields (repeated on both pages via PDF template)
        if (partII != null) {
            addIfNotBlank(fields, SA + "Page1[0].IssuersName[0]",             partII.getLine7());
            addIfNotBlank(fields, SA + "Page1[0].EIN[0]",                     partII.getLine8());
            addIfNotBlank(fields, SA + "Page1[0].DateOfIssue[0]",             partII.getLine12());
            addIfNotBlank(fields, SA + "Page1[0].ReportNumber[0].ReportNumber[0]", partII.getLine10());
        }

        // Fill rows (template supports up to 50: 25 per page)
        int limit = Math.min(rows.size(), 50);
        BigDecimal page1ColETotal = BigDecimal.ZERO;
        BigDecimal page2ColETotal = BigDecimal.ZERO;

        for (int i = 0; i < limit; i++) {
            FormScheduleA row   = rows.get(i);
            int rowOnPage       = (i % 25) + 1;
            boolean isPage1     = i < 25;

            BigDecimal colB = bd(row.getColBActualInterest());
            BigDecimal colC = bd(row.getColCCreditRateInterest());
            BigDecimal colD = computeColD(colC, bondType);
            BigDecimal colE = computeColE(colB, colC, colD, bondType);

            if (isPage1) {
                fillPage1Row(fields, rowOnPage, row.getColAMaturityDate(), colB, colC, colD, colE);
                page1ColETotal = page1ColETotal.add(colE);
            } else {
                fillPage2Row(fields, rowOnPage, row.getColAMaturityDate(), colB, colC, colD, colE);
                page2ColETotal = page2ColETotal.add(colE);
            }
        }

        // Totals
        BigDecimal grandTotal = page1ColETotal.add(page2ColETotal);
        fields.add(f(SA + "Page1[0].Ln1AmountColumnE[0]",   fmt(page1ColETotal)));
        fields.add(f(SA + "Page1[0].Ln2AmountLine4[0]",     fmt(page2ColETotal)));
        fields.add(f(SA + "Page1[0].Ln3TotalLine1Line2[0]", fmt(grandTotal)));
        if (!page2ColETotal.equals(BigDecimal.ZERO)) {
            fields.add(f(SA + "Page2[0].Ln4AmountColumnE[0]", fmt(page2ColETotal)));
        }

        return fillService.fill(resource.getContentAsByteArray(), fields);
    }

    // ── Page 1 row fill ────────────────────────────────────────────────────────
    // Page 1 field base: base = 5 * rowNum (1-indexed)

    private void fillPage1Row(List<PdfFieldValue> fields, int rowNum,
                               String colA, BigDecimal colB, BigDecimal colC,
                               BigDecimal colD, BigDecimal colE) {
        int base      = 5 * rowNum;
        String rowPfx = P1T + "BodyRow" + rowNum + "[0].";
        addIfNotBlank(fields, rowPfx + "f1_" + base + "[0]",       colA);
        addAmount    (fields, rowPfx + "f1_" + (base + 1) + "[0]", colB);
        addAmount    (fields, rowPfx + "f1_" + (base + 2) + "[0]", colC);
        addAmount    (fields, rowPfx + "f1_" + (base + 3) + "[0]", colD);
        addAmount    (fields, rowPfx + "ColumnE_" + rowNum + "[0]", colE);
    }

    // ── Page 2 row fill ────────────────────────────────────────────────────────
    // Page 2 field base: rows 1-18 → 5*(N-1)+1; rows 19-25 → 5*(N-2)+1
    // (Rows 17 and 18 share the same leaf field names in the PDF — a design quirk —
    //  but fully-qualified paths including BodyRowN[0] are distinct.)

    private void fillPage2Row(List<PdfFieldValue> fields, int rowNum,
                               String colA, BigDecimal colB, BigDecimal colC,
                               BigDecimal colD, BigDecimal colE) {
        int base      = page2Base(rowNum);
        String rowPfx = P2T + "BodyRow" + rowNum + "[0].";
        addIfNotBlank(fields, rowPfx + "f2_" + base + "[0]",       colA);
        addAmount    (fields, rowPfx + "f2_" + (base + 1) + "[0]", colB);
        addAmount    (fields, rowPfx + "f2_" + (base + 2) + "[0]", colC);
        addAmount    (fields, rowPfx + "f2_" + (base + 3) + "[0]", colD);
        addAmount    (fields, rowPfx + "ColumnE_" + rowNum + "[0]", colE);
    }

    private static int page2Base(int rowNum) {
        return rowNum <= 18 ? 5 * (rowNum - 1) + 1 : 5 * (rowNum - 2) + 1;
    }

    // ── Column computation (mirrors FormSubmissionServiceImpl logic) ────────────

    private static BigDecimal computeColD(BigDecimal colC, String bondType) {
        if ("102".equals(bondType) || "103".equals(bondType)) {
            return colC.multiply(new BigDecimal("0.70")).setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }

    private static BigDecimal computeColE(BigDecimal colB, BigDecimal colC,
                                           BigDecimal colD, String bondType) {
        if ("102".equals(bondType) || "103".equals(bondType)) {
            return colB.min(colD != null ? colD : BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        }
        return colB.min(colC).setScale(2, RoundingMode.HALF_UP);
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static BigDecimal bd(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static void addIfNotBlank(List<PdfFieldValue> fields, String path, String value) {
        if (value != null && !value.isBlank()) fields.add(f(path, value));
    }

    private static void addAmount(List<PdfFieldValue> fields, String path, BigDecimal amount) {
        if (amount != null) fields.add(f(path, fmt(amount)));
    }

    private static PdfFieldValue f(String path, String value) {
        return new PdfFieldValue(path, value != null ? value : "");
    }

    private static String fmt(BigDecimal bd) {
        return bd == null ? "" : bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
