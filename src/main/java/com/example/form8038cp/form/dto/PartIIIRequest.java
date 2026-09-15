package com.example.form8038cp.form.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class PartIIIRequest {

    /** Line 18 — Interest Payment Date. Accepts YYYY-MM-DD or MM/DD/YYYY; normalized to YYYY-MM-DD on save. */
    @Pattern(regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}|[0-9]{2}/[0-9]{2}/[0-9]{4}",
             message = "Interest payment date must be a valid date (YYYY-MM-DD or MM/DD/YYYY)")
    private String line18;

    /** Line 19a — Interest Payable Amount (positive decimal > 0, stored as String) */
    @Pattern(regexp = "\\d{1,13}(\\.\\d{1,2})?",
             message = "Interest payable amount must be a valid positive decimal")
    private String line19a;

    /**
     * Line 19b — Section 54A(b)(3) Credit Rate.
     * Pattern: ([0-9]|[1-9][0-9])\.[0-9]{2}  (e.g. "5.25" or "12.50")
     */
    @Pattern(regexp = "([0-9]|[1-9][0-9])\\.[0-9]{2}",
             message = "Credit rate must be in format D.DD or DD.DD (e.g. 5.25)")
    private String line19b;

    /** Line 19c — Total Eligible Interest Computed Amount (non-negative decimal, stored as String) */
    @Pattern(regexp = "\\d{1,13}(\\.\\d{1,2})?",
             message = "Total eligible interest amount must be a valid non-negative decimal")
    private String line19c;

    /**
     * Line 20 — Which credit line applies.
     * One of: "20A", "20B", "20C", "20D", "20E", "20F"
     */
    @Pattern(regexp = "20[A-F]", message = "Credit line type must be one of: 20A, 20B, 20C, 20D, 20E, 20F")
    private String line20Type;

    /** Line 21a — Net Increase Previous Payment Amount (positive decimal, stored as String) */
    @Pattern(regexp = "\\d{1,13}(\\.\\d{1,2})?",
             message = "Net increase amount must be a valid positive decimal")
    private String line21a;

    /** Line 21b — Net Decrease Previous Payment Amount (non-positive decimal, stored as String) */
    @Pattern(regexp = "\\d{1,13}(\\.\\d{1,2})?",
             message = "Net decrease amount must be a valid decimal")
    private String line21b;

    /** Line 21c Code — Explanation Code (one of "211" through "219") */
    @Pattern(regexp = "21[1-9]", message = "Explanation code must be between 211 and 219")
    private String line21cCode;

    /** Line 21c Date — optional text (max 10 chars) */
    @Size(max = 10, message = "Line 21c date text must not exceed 10 characters")
    private String line21cDate;

    /** Line 22 — Credit Payment Requested Amount (non-negative, REQUIRED at submit time) */
    @DecimalMin(value = "0.00", message = "Credit payment requested amount must be zero or greater")
    private BigDecimal line22;

    /** Line 23a — Debt Service Schedule Change Indicator ("true" or "false") */
    @Pattern(regexp = "true|false", message = "Line 23a must be \"true\" or \"false\"")
    private String line23a;

    /** Line 23b — Explanation Code (one of "231" through "239") */
    @Pattern(regexp = "23[1-9]", message = "Explanation code must be between 231 and 239")
    private String line23b;

    /** Line 24a — Interest Paid Before Payment Date Indicator ("true" or "false") */
    @Pattern(regexp = "true|false", message = "Line 24a must be \"true\" or \"false\"")
    private String line24a;

    /** Line 24b — Explanation Code (one of "241" through "249") */
    @Pattern(regexp = "24[1-9]", message = "Explanation code must be between 241 and 249")
    private String line24b;

    /** Line 25 — Final Interest Payment Date Indicator ("true" or "false") */
    @Pattern(regexp = "true|false", message = "Line 25 must be \"true\" or \"false\"")
    private String line25;
}
