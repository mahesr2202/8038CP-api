package com.example.form8038cp.form.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PartIIRequest {

    /** Line 7 — Issuer name, or "SAME" if same as entity receiving payment */
    @Size(max = 80, message = "Issuer name must not exceed 80 characters")
    private String line7;

    /** Line 8 — Reporting Authority EIN: 9 bare digits or XX-XXXXXXX format. */
    @Pattern(regexp = "[0-9]{9}|[0-9]{2}-[0-9]{7}",
             message = "Reporting authority EIN must be 9 digits or in XX-XXXXXXX format")
    private String line8;

    /** Line 9 — Reporting Authority Street Address (max 35) */
    @Size(max = 35, message = "Street address must not exceed 35 characters")
    private String line9Street;

    @Size(max = 10, message = "Room/suite must not exceed 10 characters")
    private String line9Room;

    /** Line 10 — Report Number (pattern [248][0-9][0-9]) */
    @Pattern(regexp = "[248][0-9][0-9]", message = "Report number must match pattern [248]XX (e.g. 8XX, 4XX, 2XX)")
    private String line10;

    /** Line 11 — (unused / extra address line) */
    @Size(max = 60, message = "Line 11 must not exceed 60 characters")
    private String line11;

    /** Line 12 — Bond Issue Date. Accepts YYYY-MM-DD or MM/DD/YYYY; normalized to YYYY-MM-DD on save. */
    @Pattern(regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}|[0-9]{2}/[0-9]{2}/[0-9]{4}",
             message = "Bond issue date must be a valid date (YYYY-MM-DD or MM/DD/YYYY)")
    private String line12;

    /** Line 13 — Bond Issue Name (max 35) */
    @Size(max = 35, message = "Bond issue name must not exceed 35 characters")
    private String line13;

    /** Line 14 — CUSIP Number ([A-Za-z0-9]{9}) OR "NONE" */
    @Pattern(regexp = "[A-Za-z0-9]{9}|NONE",
             message = "CUSIP number must be exactly 9 alphanumeric characters, or \"NONE\"")
    private String line14;

    /** Line 15a — Contact Person Name (PersonNameType, max 35) */
    @Size(max = 35, message = "Contact person name must not exceed 35 characters")
    @Pattern(regexp = "([A-Za-z0-9'\\-] ?)*[A-Za-z0-9'\\-]",
             message = "Contact person name contains invalid characters")
    private String line15;

    /** Line 15b — Contact Person Title (PersonTitleType, max 35) */
    @Size(max = 35, message = "Contact person title must not exceed 35 characters")
    private String line15Title;

    /** Line 16 — Contact Phone. Accepts 10 bare digits or common US formats. */
    @Pattern(regexp = "[0-9]{10}|\\(?[0-9]{3}\\)?[\\s.\\-]?[0-9]{3}[\\s.\\-]?[0-9]{4}",
             message = "Phone number must be 10 digits (e.g. 5551234567 or (555) 123-4567)")
    private String line16;

    /** Line 17a — Bond Rate Type: "VARIABLE" or "FIXED" */
    @Pattern(regexp = "VARIABLE|FIXED", message = "Bond rate type must be \"VARIABLE\" or \"FIXED\"")
    private String line17a;

    /** Line 17b — Issue Price Amount (decimal up to 9999999999999.99) */
    @Pattern(regexp = "\\d{1,13}(\\.\\d{2})?",
             message = "Issue price must be a valid decimal amount (up to 13 digits with optional 2 decimal places)")
    private String line17b;

    /** Line 17c — Bond Type Code (one of: 102, 103, 104, 105, 109, 110) */
    @Pattern(regexp = "102|103|104|105|109|110",
             message = "Bond type code must be one of: 102, 103, 104, 105, 109, 110")
    private String line17c;
}
