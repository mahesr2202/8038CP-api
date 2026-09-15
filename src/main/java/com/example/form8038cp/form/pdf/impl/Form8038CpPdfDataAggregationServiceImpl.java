package com.example.form8038cp.form.pdf.impl;

import com.example.form8038cp.form.entity.FormDirectDeposit;
import com.example.form8038cp.form.entity.FormPaidPreparer;
import com.example.form8038cp.form.entity.FormPartI;
import com.example.form8038cp.form.entity.FormPartII;
import com.example.form8038cp.form.entity.FormPartIII;
import com.example.form8038cp.form.entity.FormSignature;
import com.example.form8038cp.form.entity.FormSubmission;
import com.example.form8038cp.form.pdf.Form8038CpPdfDataAggregationService;
import com.example.form8038cp.form.pdf.PdfFieldValue;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
public class Form8038CpPdfDataAggregationServiceImpl implements Form8038CpPdfDataAggregationService {

    private static final String P1 = "topmostSubform[0].Page1[0].";

    @Override
    public List<PdfFieldValue> aggregateMainForm(
            FormSubmission submission,
            FormPartI partI,
            FormPartII partII,
            FormPartIII partIII,
            FormDirectDeposit deposit,
            FormSignature sig,
            FormPaidPreparer preparer) {

        List<PdfFieldValue> fields = new ArrayList<>();

        // Header
        if (Boolean.TRUE.equals(submission.getIsAmended())) {
            fields.add(f(P1 + "Page1Header[0].AmendedReturn[0]", "true"));
        }

        // Part I — Entity Receiving Payment
        if (partI != null) {
            addIfNotBlank(fields, P1 + "f1_1[0]", partI.getLine1());
            addIfNotBlank(fields, P1 + "f1_2[0]", partI.getLine2());
            addIfNotBlank(fields, P1 + "f1_3[0]", partI.getLine3Street());
            addIfNotBlank(fields, P1 + "f1_3_1[0]", partI.getLine3Room());
            addIfNotBlank(fields, P1 + "f1_4[0]", partI.getLine4());
            addIfNotBlank(fields, P1 + "f1_5[0]", partI.getLine5());
            addIfNotBlank(fields, P1 + "f1_6[0]", partI.getLine5Title());
            addIfNotBlank(fields, P1 + "f1_7[0]", partI.getLine6());
        }

        // Part II — Reporting Authority
        // Note: the PDF template's f1_9 maps to the issuer's street address (line 9).
        // Line 8 EIN does not have a dedicated fillable AcroForm field in this template version.
        if (partII != null) {
            addIfNotBlank(fields, P1 + "f1_8[0]", partII.getLine7());
            addIfNotBlank(fields, P1 + "f1_9[0]", partII.getLine9Street());
            addIfNotBlank(fields, P1 + "f1_9_1[0]", partII.getLine9Room());
            addIfNotBlank(fields, P1 + "f1_10[0]", partII.getLine10());
            addIfNotBlank(fields, P1 + "f1_11[0]", partII.getLine11());
            addIfNotBlank(fields, P1 + "f1_12[0]", partII.getLine12());
            addIfNotBlank(fields, P1 + "f1_13[0]", partII.getLine13());
            addIfNotBlank(fields, P1 + "f1_14[0]", partII.getLine14());
            // f1_15 covers the "Name and title" box — combine name + title into one field
            String nameTitle = combine(partII.getLine15(), partII.getLine15Title(), " / ");
            addIfNotBlank(fields, P1 + "f1_15[0]", nameTitle);
            addIfNotBlank(fields, P1 + "f1_16[0]", partII.getLine16());

            if ("VARIABLE".equalsIgnoreCase(partII.getLine17a())) {
                fields.add(f(P1 + "c1_17a_variable[0]", "true"));
            } else if ("FIXED".equalsIgnoreCase(partII.getLine17a())) {
                fields.add(f(P1 + "c1_17a_fixed[0]", "true"));
            }
            addIfNotBlank(fields, P1 + "f1_17b[0]", partII.getLine17b());
            addIfNotBlank(fields, P1 + "f1_17c[0]", partII.getLine17c());
        }

        // Part III — Payment of Credit
        if (partIII != null) {
            addIfNotBlank(fields, P1 + "f1_18[0]", partIII.getLine18());
            addIfNotBlank(fields, P1 + "f1_19a[0]", partIII.getLine19a());
            splitRate(fields, partIII.getLine19b(), P1 + "f1_19b_1[0]", P1 + "f1_19b_2[0]");
            addIfNotBlank(fields, P1 + "f1_19c[0]", partIII.getLine19c());

            fillLine20(fields, partIII);

            addIfNotBlank(fields, P1 + "f1_21a[0]", partIII.getLine21a());
            addIfNotBlank(fields, P1 + "f1_21b[0]", partIII.getLine21b());
            addIfNotBlank(fields, P1 + "f1_21c[0]", partIII.getLine21cCode());

            if (partIII.getLine22() != null) {
                fields.add(f(P1 + "f1_22[0]", fmt(partIII.getLine22())));
            }

            fillYesNo(fields, partIII.getLine23a(), P1 + "c1_23[0]", P1 + "c1_23[1]");
            addIfNotBlank(fields, P1 + "f1_23b[0]", partIII.getLine23b());

            fillYesNo(fields, partIII.getLine24a(), P1 + "c1_24[0]", P1 + "c1_24[1]");
            addIfNotBlank(fields, P1 + "f1_24b[0]", partIII.getLine24b());

            fillYesNo(fields, partIII.getLine25(), P1 + "c1_25[0]", P1 + "c1_25[1]");
        }

        // Direct Deposit (line 26)
        if (deposit != null) {
            addIfNotBlank(fields, P1 + "RoutingNumberComb[0].f1_27a[0]", deposit.getRoutingNumber());
            if ("CHECKING".equalsIgnoreCase(deposit.getAccountType())) {
                fields.add(f(P1 + "c1_27b_1[0]", "true"));
            } else if ("SAVINGS".equalsIgnoreCase(deposit.getAccountType())) {
                fields.add(f(P1 + "c1_27b_2[0]", "true"));
            }
            addIfNotBlank(fields, P1 + "AccountNumberComb[0].f1_27c[0]", deposit.getAccountNumber());
        }

        // Signature section
        if (sig != null) {
            addIfNotBlank(fields, P1 + "NameTitle[0]", sig.getSigNameTitle());
        }

        // Paid Preparer Use Only
        if (preparer != null) {
            addIfNotBlank(fields, P1 + "PrintTypePre[0]", preparer.getPrepName());
            if (Boolean.TRUE.equals(preparer.getPrepSelfEmployed())) {
                fields.add(f(P1 + "CheckSelfEmployed[0]", "true"));
            }
            addIfNotBlank(fields, P1 + "PTIN[0]", preparer.getPrepPtin());
            addIfNotBlank(fields, P1 + "FirmsName[0]", preparer.getPrepFirmName());
            addIfNotBlank(fields, P1 + "FirmsEIN[0]", preparer.getPrepFirmEin());
            addIfNotBlank(fields, P1 + "FirmsAddress[0]", preparer.getPrepFirmAddress());
            addIfNotBlank(fields, P1 + "PhoneNumber[0]", preparer.getPrepPhone());
        }

        return fields;
    }

    // ── Line 20 computation ────────────────────────────────────────────────────

    private void fillLine20(List<PdfFieldValue> fields, FormPartIII partIII) {
        if (partIII.getLine20Type() == null || partIII.getLine19a() == null) return;
        try {
            BigDecimal line19a = new BigDecimal(partIII.getLine19a().replaceAll("[^0-9.]", ""));
            BigDecimal line19c = BigDecimal.ZERO;
            if (partIII.getLine19c() != null && !partIII.getLine19c().isBlank()) {
                line19c = new BigDecimal(partIII.getLine19c().replaceAll("[^0-9.]", ""));
            }

            String type = partIII.getLine20Type().toUpperCase();
            BigDecimal creditAmt = switch (type) {
                case "20A" -> line19a.multiply(new BigDecimal("0.35")).setScale(2, RoundingMode.HALF_UP);
                case "20B" -> line19a.multiply(new BigDecimal("0.45")).setScale(2, RoundingMode.HALF_UP);
                case "20C", "20D", "20E", "20F" -> line19a.min(line19c).setScale(2, RoundingMode.HALF_UP);
                default -> null;
            };

            if (creditAmt == null) return;

            String fieldPath = switch (type) {
                case "20A" -> P1 + "f1_20a[0]";
                case "20B" -> P1 + "f1_20b[0]";
                case "20C" -> P1 + "f1_20c[0]";
                case "20D" -> P1 + "f1_20d[0]";
                case "20E" -> P1 + "f1_20e[0]";
                case "20F" -> P1 + "f1_20f[0]";
                default -> null;
            };
            if (fieldPath != null) {
                fields.add(f(fieldPath, fmt(creditAmt)));
            }
        } catch (NumberFormatException ignored) {}
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private static void splitRate(List<PdfFieldValue> fields, String rate, String path1, String path2) {
        if (rate == null || rate.isBlank()) return;
        int dotIdx = rate.indexOf('.');
        if (dotIdx >= 0) {
            fields.add(f(path1, rate.substring(0, dotIdx)));
            fields.add(f(path2, rate.substring(dotIdx + 1)));
        } else {
            fields.add(f(path1, rate));
        }
    }

    private static void fillYesNo(List<PdfFieldValue> fields, String value, String yesPath, String noPath) {
        if (value == null) return;
        if ("true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value)) {
            fields.add(f(yesPath, "true"));
        } else if ("false".equalsIgnoreCase(value) || "no".equalsIgnoreCase(value)) {
            fields.add(f(noPath, "true"));
        }
    }

    private static void addIfNotBlank(List<PdfFieldValue> fields, String path, String value) {
        if (value != null && !value.isBlank()) {
            fields.add(f(path, value));
        }
    }

    private static PdfFieldValue f(String path, String value) {
        return new PdfFieldValue(path, value != null ? value : "");
    }

    private static String fmt(BigDecimal bd) {
        return bd == null ? "" : bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String combine(String a, String b, String sep) {
        if (a != null && !a.isBlank() && b != null && !b.isBlank()) return a + sep + b;
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
