package com.example.form8038cp.form.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DirectDepositRequest {

    /**
     * Line 26a — ABA Routing Transit Number.
     * RoutingTransitNumberType: (01|02|...|32)[0-9]{7} — two-digit prefix then 7 digits.
     */
    @Pattern(
        regexp = "(0[1-9]|1[0-2]|2[1-9]|3[0-2])[0-9]{7}",
        message = "Routing number must be a valid 9-digit ABA routing transit number"
    )
    private String routingNumber;

    /**
     * Line 26b — Account Type.
     * BankAccountType: "1" (checking) or "2" (savings).
     */
    @Pattern(regexp = "[12]", message = "Account type must be \"1\" (checking) or \"2\" (savings)")
    private String accountType;

    /**
     * Line 26c — Account Number.
     * BankAccountNumberType: max 17 chars, pattern [A-Za-z0-9\-]+
     */
    @Size(max = 17, message = "Account number must not exceed 17 characters")
    @Pattern(regexp = "[A-Za-z0-9\\-]+", message = "Account number may only contain letters, digits, and hyphens")
    private String accountNumber;
}
