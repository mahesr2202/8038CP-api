package com.example.form8038cp.form.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter @Builder
public class ScheduleARowResponse {
    private int rowOrder;
    private String colAMaturityDate;
    private BigDecimal colBActualInterest;
    private BigDecimal colCCreditRateInterest;
    /** Column (d): col_c × 70%. Populated only for bond types 102 and 103; null for 104/105. */
    private BigDecimal colD70PctAmount;
    /** Column (e): eligible interest = min(b, d) for 102/103; min(b, c) for 104/105. */
    private BigDecimal colEEligibleInterest;
    /** Running total of column (e) — included on the last row for convenience. */
    private BigDecimal line19cTotal;
}
