package com.example.form8038cp.form.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PartIRequest {

    /** Line 1 — Business Name (BusinessNameLine1Type, max 75) */
    @Size(max = 75, message = "Business name must not exceed 75 characters")
    private String line1;

    /** Line 2 — EIN: 9 bare digits (123456789) or IRS-formatted XX-XXXXXXX (12-3456789). */
    @Pattern(regexp = "[0-9]{9}|[0-9]{2}-[0-9]{7}",
             message = "EIN must be 9 digits or in XX-XXXXXXX format")
    private String line2;

    /** Lines 3-4 — US Address */
    @Size(max = 35, message = "Street address must not exceed 35 characters")
    private String line3Street;

    @Size(max = 10, message = "Room/suite must not exceed 10 characters")
    private String line3Room;

    /** Line 4 — City, State, ZIP (stored as one field; city max 22, state 2, ZIP validated separately) */
    @Size(max = 60, message = "City/State/ZIP line must not exceed 60 characters")
    private String line4;

    /** Line 5a — Contact Person Name (PersonNameType, max 35) */
    @Size(max = 35, message = "Contact person name must not exceed 35 characters")
    @Pattern(regexp = "([A-Za-z0-9'\\-] ?)*[A-Za-z0-9'\\-]",
             message = "Contact person name contains invalid characters")
    private String line5;

    /** Line 5b — Contact Person Title (PersonTitleType, max 35) */
    @Size(max = 35, message = "Contact person title must not exceed 35 characters")
    private String line5Title;

    /** Line 6 — Contact Phone. Accepts 10 bare digits or common US formats (parentheses, dashes, dots). */
    @Pattern(regexp = "[0-9]{10}|\\(?[0-9]{3}\\)?[\\s.\\-]?[0-9]{3}[\\s.\\-]?[0-9]{4}",
             message = "Phone number must be 10 digits (e.g. 5551234567 or (555) 123-4567)")
    private String line6;
}
