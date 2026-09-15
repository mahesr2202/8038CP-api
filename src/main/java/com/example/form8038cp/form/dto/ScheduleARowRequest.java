package com.example.form8038cp.form.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter @Setter
public class ScheduleARowRequest {

    /** Column (a) — Bond maturity date */
    @NotNull(message = "Bond maturity date (column a) is required")
    @Pattern(regexp = "[0-9]{4}-[0-9]{2}-[0-9]{2}|[0-9]{2}/[0-9]{2}/[0-9]{4}",
             message = "Bond maturity date must be YYYY-MM-DD or MM/DD/YYYY")
    private String colAMaturityDate;

    /** Column (b) — Actual interest paid on this maturity on the line 18 interest payment date */
    @NotNull(message = "Actual interest (column b) is required")
    @DecimalMin(value = "0.00", message = "Actual interest must be zero or greater")
    private BigDecimal colBActualInterest;

    /** Column (c) — Interest recalculated using the line 19b applicable credit rate */
    @NotNull(message = "Credit-rate interest (column c) is required")
    @DecimalMin(value = "0.00", message = "Credit-rate interest must be zero or greater")
    private BigDecimal colCCreditRateInterest;
}
