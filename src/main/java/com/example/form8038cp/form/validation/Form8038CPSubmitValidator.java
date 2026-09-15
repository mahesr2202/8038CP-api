package com.example.form8038cp.form.validation;

import com.example.form8038cp.exception.FieldErrorDetail;
import com.example.form8038cp.exception.ValidationException;
import com.example.form8038cp.form.entity.FormDirectDeposit;
import com.example.form8038cp.form.entity.FormPartII;
import com.example.form8038cp.form.entity.FormPartIII;
import com.example.form8038cp.form.entity.FormScheduleA;
import com.example.form8038cp.form.entity.FormSignature;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Submit-time cross-field validator for Form 8038-CP.
 * Collects all errors before throwing, providing full feedback in a single pass.
 */
@Component
public class Form8038CPSubmitValidator {

    private static final LocalDate BOND_ISSUE_MIN = LocalDate.of(2009, 2, 17);
    private static final LocalDate BOND_ISSUE_MAX = LocalDate.of(2017, 12, 31);
    private static final LocalDate INTEREST_PAYMENT_MIN = LocalDate.of(2009, 2, 17);

    private static final Set<String> BOND_TYPES_REQUIRING_19B_19C =
            Set.of("102", "103", "104", "105");

    /**
     * Validate all required fields and cross-field rules at submit time.
     * Throws {@link ValidationException} with the full list of errors if any are found.
     *
     * @param partII       the saved Part II entity (may be null if never saved)
     * @param partIII      the saved Part III entity (may be null if never saved)
     * @param directDeposit the saved direct deposit entity (optional section)
     * @param signature    the saved signature entity (may be null if never saved)
     */
    public void validate(FormPartII partII,
                         FormPartIII partIII,
                         FormDirectDeposit directDeposit,
                         FormSignature signature,
                         List<FormScheduleA> scheduleARows) {

        List<FieldErrorDetail> errors = new ArrayList<>();

        validatePartII(partII, errors);
        validatePartIII(partIII, errors);
        validateDirectDeposit(directDeposit, errors);
        validateSignature(signature, errors);
        validateCrossSection(partII, partIII, scheduleARows, errors);

        if (!errors.isEmpty()) {
            throw new ValidationException("We found problems that need your attention.", errors);
        }
    }

    // -------------------------------------------------------------------------
    // Part II validation
    // -------------------------------------------------------------------------

    private void validatePartII(FormPartII p2, List<FieldErrorDetail> errors) {
        if (p2 == null) {
            errors.add(FieldErrorDetail.of("REQUIRED", "partII",
                    "Part II (Reporting Authority) is required before submitting"));
            return; // no point checking individual fields
        }

        // Line 10 — Report Number
        if (isBlank(p2.getLine10())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line10",
                    "Report number (line 10) is required"));
        }

        // Line 12 — Bond Issue Date + range check
        if (isBlank(p2.getLine12())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line12",
                    "Bond issue date (line 12) is required"));
        } else {
            LocalDate bondDate = parseDate(p2.getLine12());
            if (bondDate == null) {
                errors.add(FieldErrorDetail.of("INVALID_FORMAT", "line12",
                        "Bond issue date must be in YYYY-MM-DD format"));
            } else if (bondDate.isBefore(BOND_ISSUE_MIN) || bondDate.isAfter(BOND_ISSUE_MAX)) {
                errors.add(FieldErrorDetail.of("INVALID_RANGE", "line12",
                        "Bond issue date must be between 2009-02-17 and 2017-12-31"));
            }
        }

        // Line 13 — Bond Issue Name
        if (isBlank(p2.getLine13())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line13",
                    "Bond issue name (line 13) is required"));
        }

        // Line 15 — Contact Person Name
        if (isBlank(p2.getLine15())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line15",
                    "Contact person name (line 15) is required"));
        }

        // Line 16 — Contact Phone
        if (isBlank(p2.getLine16())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line16",
                    "Contact person phone (line 16) is required"));
        }

        // Line 17a — Bond Rate Type (VARIABLE or FIXED)
        if (isBlank(p2.getLine17a())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line17a",
                    "Bond rate type (line 17a) is required — must be \"VARIABLE\" or \"FIXED\""));
        }
    }

    // -------------------------------------------------------------------------
    // Part III validation
    // -------------------------------------------------------------------------

    private void validatePartIII(FormPartIII p3, List<FieldErrorDetail> errors) {
        if (p3 == null) {
            errors.add(FieldErrorDetail.of("REQUIRED", "partIII",
                    "Part III (Payment of Credit) is required before submitting"));
            return;
        }

        // Line 18 — Interest Payment Date + minimum date check
        if (isBlank(p3.getLine18())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line18",
                    "Interest payment date (line 18) is required"));
        } else {
            LocalDate interestDate = parseDate(p3.getLine18());
            if (interestDate == null) {
                errors.add(FieldErrorDetail.of("INVALID_FORMAT", "line18",
                        "Interest payment date must be in YYYY-MM-DD format"));
            } else if (interestDate.isBefore(INTEREST_PAYMENT_MIN)) {
                errors.add(FieldErrorDetail.of("INVALID_RANGE", "line18",
                        "Interest payment date must be on or after 2009-02-17"));
            }
        }

        // Line 19a — Interest Payable Amount (required, > 0)
        if (isBlank(p3.getLine19a())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line19a",
                    "Interest payable amount (line 19a) is required"));
        } else {
            try {
                BigDecimal amt = new BigDecimal(p3.getLine19a());
                if (amt.compareTo(BigDecimal.ZERO) <= 0) {
                    errors.add(FieldErrorDetail.of("INVALID_VALUE", "line19a",
                            "Interest payable amount must be greater than zero"));
                }
            } catch (NumberFormatException e) {
                errors.add(FieldErrorDetail.of("INVALID_FORMAT", "line19a",
                        "Interest payable amount must be a valid decimal number"));
            }
        }

        // Line 20 — Credit line type (required)
        if (isBlank(p3.getLine20Type())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line20Type",
                    "Credit line type (line 20) is required — must be one of: 20A, 20B, 20C, 20D, 20E, 20F"));
        }

        // Line 22 — Credit Payment Requested Amount (required, non-negative)
        if (p3.getLine22() == null) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line22",
                    "Credit payment requested amount (line 22) is required"));
        } else if (p3.getLine22().compareTo(BigDecimal.ZERO) < 0) {
            errors.add(FieldErrorDetail.of("INVALID_VALUE", "line22",
                    "Credit payment requested amount must be zero or greater"));
        }

        // Line 23a — Debt Service Schedule Change Indicator (required)
        if (isBlank(p3.getLine23a())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line23a",
                    "Debt service schedule change indicator (line 23a) is required"));
        }

        // Line 24a — Interest Paid Before Payment Date Indicator (required)
        if (isBlank(p3.getLine24a())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line24a",
                    "Interest paid before payment date indicator (line 24a) is required"));
        }

        // Line 25 — Final Interest Payment Date Indicator (required)
        if (isBlank(p3.getLine25())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "line25",
                    "Final interest payment date indicator (line 25) is required"));
        }

        // Cross-field: line21a and line21b are mutually exclusive
        boolean has21a = !isBlank(p3.getLine21a());
        boolean has21b = !isBlank(p3.getLine21b());
        if (has21a && has21b) {
            errors.add(FieldErrorDetail.of("MUTUAL_EXCLUSION", "line21a",
                    "Line 21a (net increase) and line 21b (net decrease) cannot both be present"));
        }

        // Cross-field: if 21a or 21b present, 21cCode should be present
        if ((has21a || has21b) && isBlank(p3.getLine21cCode())) {
            errors.add(FieldErrorDetail.of("REQUIRED_CONDITIONAL", "line21cCode",
                    "Explanation code (line 21c) is required when line 21a or 21b is provided"));
        }

        // Cross-field: if line23a = "true", line23b should be present
        if ("true".equals(p3.getLine23a()) && isBlank(p3.getLine23b())) {
            errors.add(FieldErrorDetail.of("REQUIRED_CONDITIONAL", "line23b",
                    "Explanation code (line 23b) is required when line 23a is \"true\""));
        }

        // Cross-field: if line24a = "false", line24b should be present
        if ("false".equals(p3.getLine24a()) && isBlank(p3.getLine24b())) {
            errors.add(FieldErrorDetail.of("REQUIRED_CONDITIONAL", "line24b",
                    "Explanation code (line 24b) is required when line 24a is \"false\""));
        }
    }

    // -------------------------------------------------------------------------
    // Direct Deposit validation (optional section, but if partially filled must be complete)
    // -------------------------------------------------------------------------

    private void validateDirectDeposit(FormDirectDeposit dep, List<FieldErrorDetail> errors) {
        if (dep == null) {
            return; // direct deposit is optional
        }
        boolean hasRouting = !isBlank(dep.getRoutingNumber());
        boolean hasType    = !isBlank(dep.getAccountType());
        boolean hasNumber  = !isBlank(dep.getAccountNumber());

        // If any direct-deposit field is present, all three are required
        if (hasRouting || hasType || hasNumber) {
            if (!hasRouting) {
                errors.add(FieldErrorDetail.of("REQUIRED_CONDITIONAL", "routingNumber",
                        "Routing number is required when direct deposit information is provided"));
            } else if (!isValidRtn(dep.getRoutingNumber())) {
                errors.add(FieldErrorDetail.of("INVALID_RTN", "routingNumber",
                        "Routing number is invalid — must be 9 digits and pass the ABA check-digit algorithm"));
            }
            if (!hasType) {
                errors.add(FieldErrorDetail.of("REQUIRED_CONDITIONAL", "accountType",
                        "Account type is required when direct deposit information is provided"));
            }
            if (!hasNumber) {
                errors.add(FieldErrorDetail.of("REQUIRED_CONDITIONAL", "accountNumber",
                        "Account number is required when direct deposit information is provided"));
            }
        }
    }

    private boolean isValidRtn(String rtn) {
        if (rtn == null || rtn.length() != 9 || !rtn.chars().allMatch(Character::isDigit)) return false;
        int sum = 3 * (rtn.charAt(0) - '0' + rtn.charAt(3) - '0' + rtn.charAt(6) - '0')
                + 7 * (rtn.charAt(1) - '0' + rtn.charAt(4) - '0' + rtn.charAt(7) - '0')
                +     (rtn.charAt(2) - '0' + rtn.charAt(5) - '0' + rtn.charAt(8) - '0');
        return sum % 10 == 0;
    }

    // -------------------------------------------------------------------------
    // Signature validation
    // -------------------------------------------------------------------------

    private void validateSignature(FormSignature sig, List<FieldErrorDetail> errors) {
        if (sig == null) {
            errors.add(FieldErrorDetail.of("REQUIRED", "signature",
                    "Issuer signature is required before submitting"));
            return;
        }

        if (isBlank(sig.getSigSignature())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "sigSignature",
                    "Signer name is required"));
        }

        if (isBlank(sig.getSigDate())) {
            errors.add(FieldErrorDetail.of("REQUIRED", "sigDate",
                    "Signature date is required"));
        } else {
            LocalDate sigDate = parseDate(sig.getSigDate());
            if (sigDate == null) {
                errors.add(FieldErrorDetail.of("INVALID_FORMAT", "sigDate",
                        "Signature date must be in YYYY-MM-DD format"));
            }
        }
    }

    // -------------------------------------------------------------------------
    // Cross-section validation (Part II vs Part III)
    // -------------------------------------------------------------------------

    private void validateCrossSection(FormPartII p2, FormPartIII p3,
                                      List<FormScheduleA> scheduleARows,
                                      List<FieldErrorDetail> errors) {
        if (p2 == null || p3 == null) {
            return; // already reported missing sections above
        }

        String bondType  = p2.getLine17c();
        String line20Type = p3.getLine20Type();

        // Rule: if bond type requires 19b and 19c, those must be present
        if (bondType != null && BOND_TYPES_REQUIRING_19B_19C.contains(bondType)) {
            if (isBlank(p3.getLine19b())) {
                errors.add(FieldErrorDetail.of("REQUIRED_CONDITIONAL", "line19b",
                        "Section 54A(b)(3) credit rate (line 19b) is required for bond type " + bondType));
            }
            if (isBlank(p3.getLine19c())) {
                errors.add(FieldErrorDetail.of("REQUIRED_CONDITIONAL", "line19c",
                        "Total eligible interest computed amount (line 19c) is required for bond type " + bondType));
            }
            if (scheduleARows == null || scheduleARows.isEmpty()) {
                errors.add(FieldErrorDetail.of("REQUIRED_CONDITIONAL", "scheduleA",
                        "Schedule A (per-maturity interest rows) is required for bond type " + bondType
                        + " — save at least one row via PUT /forms/{id}/schedule-a"));
            }
        }

        // Rule: bond type → credit line consistency (lenient — just check line20Type is valid if bond type given)
        if (bondType != null && line20Type != null) {
            String expected = expectedCreditLine(bondType);
            if (expected != null && !line20Type.matches(expected)) {
                errors.add(FieldErrorDetail.of("INCONSISTENT", "line20Type",
                        "Credit line type " + line20Type + " is not consistent with bond type " + bondType
                        + " (expected: " + expected.replace("|", " or ") + ")"));
            }
        }
    }

    /**
     * Returns a regex fragment of acceptable credit lines for the given bond type,
     * or null if no specific constraint applies.
     */
    private String expectedCreditLine(String bondType) {
        return switch (bondType) {
            case "109" -> "20A";
            case "110" -> "20B";
            case "102" -> "20C";
            case "103" -> "20D";
            case "104" -> "20E";
            case "105" -> "20F";
            default    -> null;
        };
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private LocalDate parseDate(String value) {
        if (value == null) return null;
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
