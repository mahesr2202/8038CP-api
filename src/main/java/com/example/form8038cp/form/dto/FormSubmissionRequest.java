package com.example.form8038cp.form.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class FormSubmissionRequest {
    private Boolean isAmended;
    // Part I
    private String line1, line2, line3Street, line3Room, line4, line5, line6;
    // Part II
    private String line7, line8, line9Street, line9Room, line10, line11, line12;
    private String line13, line14, line15, line16, line17a, line17b, line17c;
    // Part III
    private String line18, line19a, line19b, line19c;
    private String line21a, line21b, line21cCode, line21cDate;
    private BigDecimal line22;
    private String line23a, line23b, line24a, line24b, line25;
    // Direct Deposit
    private String routingNumber, accountType, accountNumber;
    // Signature
    private String sigSignature, sigDate, sigNameTitle;
    // Paid Preparer
    private String prepName, prepSignature, prepDate;
    private Boolean prepSelfEmployed;
    private String prepPtin, prepFirmName, prepFirmEin, prepPhone, prepFirmAddress;
    // Payment
    private String stripePaymentIntentId;
    private String paymentStatus;
}
