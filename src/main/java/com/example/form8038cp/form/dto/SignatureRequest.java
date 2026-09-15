package com.example.form8038cp.form.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignatureRequest {

    /**
     * Signer name (PersonNm / PersonNameType, max 35 chars).
     * Maps to IssuerSignatureGrp / PersonNm in the ReturnHeader.
     */
    @Size(max = 35, message = "Signer name must not exceed 35 characters")
    @Pattern(regexp = "([A-Za-z0-9'\\-] ?)*[A-Za-z0-9'\\-]",
             message = "Signer name contains invalid characters")
    private String sigSignature;

    /** Signature date. Accepts YYYY-MM-DD or MM/DD/YYYY; normalized to YYYY-MM-DD on save. */
    @Pattern(regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}|[0-9]{2}/[0-9]{2}/[0-9]{4}",
             message = "Signature date must be a valid date (YYYY-MM-DD or MM/DD/YYYY)")
    private String sigDate;

    /**
     * Signer title (PersonTitleTxt, max 35 chars).
     */
    @Size(max = 35, message = "Signer title must not exceed 35 characters")
    private String sigNameTitle;

    /** 5-digit self-selected taxpayer PIN required for PIN Number signature option. */
    @Pattern(regexp = "\\d{5}", message = "Taxpayer PIN must be exactly 5 digits")
    private String taxpayerPin;

    @Size(max = 35, message = "First name must not exceed 35 characters")
    private String sigFirstName;

    @Size(max = 35, message = "Last name must not exceed 35 characters")
    private String sigLastName;
}
