package com.example.form8038cp.form.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PaidPreparerRequest {

    /** Preparer name (PreparerPersonNm, max 35 chars) */
    @Size(max = 35, message = "Preparer name must not exceed 35 characters")
    @Pattern(regexp = "([A-Za-z0-9'\\-] ?)*[A-Za-z0-9'\\-]",
             message = "Preparer name contains invalid characters")
    private String prepName;

    /** Preparer signature / printed name (max 35 chars) */
    @Size(max = 35, message = "Preparer signature must not exceed 35 characters")
    private String prepSignature;

    /** Preparation date. Accepts YYYY-MM-DD or MM/DD/YYYY; normalized to YYYY-MM-DD on save. */
    @Pattern(regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}|[0-9]{2}/[0-9]{2}/[0-9]{4}",
             message = "Preparation date must be a valid date (YYYY-MM-DD or MM/DD/YYYY)")
    private String prepDate;

    /** Self-employed flag */
    private Boolean prepSelfEmployed;

    /**
     * PTIN (PTINType: P[0-9]{8}) OR SSN (9 digits).
     * Accepts "P" followed by 8 digits, or 9 bare digits.
     */
    @Pattern(regexp = "P[0-9]{8}|[0-9]{9}",
             message = "PTIN must be in format P12345678 or SSN must be 9 digits")
    private String prepPtin;

    /** Firm name (BusinessNameLine1Type, max 75 chars) */
    @Size(max = 75, message = "Firm name must not exceed 75 characters")
    private String prepFirmName;

    /** Firm EIN: 9 bare digits or XX-XXXXXXX format. */
    @Pattern(regexp = "[0-9]{9}|[0-9]{2}-[0-9]{7}",
             message = "Firm EIN must be 9 digits or in XX-XXXXXXX format")
    private String prepFirmEin;

    /** Preparer phone. Accepts 10 bare digits or common US formats. */
    @Pattern(regexp = "[0-9]{10}|\\(?[0-9]{3}\\)?[\\s.\\-]?[0-9]{3}[\\s.\\-]?[0-9]{4}",
             message = "Phone number must be 10 digits (e.g. 5551234567 or (555) 123-4567)")
    private String prepPhone;

    /** Firm address (max 80 chars per entity column) */
    @Size(max = 80, message = "Firm address must not exceed 80 characters")
    private String prepFirmAddress;
}
