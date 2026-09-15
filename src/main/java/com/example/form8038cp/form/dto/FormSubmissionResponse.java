package com.example.form8038cp.form.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Getter
@Builder
public class FormSubmissionResponse {
    private Long id;
    private Long userId;
    private String status;
    private String paymentStatus;
    private OffsetDateTime submittedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private Boolean isAmended;
    // Part I
    private String line1, line2, line3Street, line3Room, line4, line5, line5Title, line6;
    // Part II
    private String line7, line8, line9Street, line9Room, line10, line11, line12;
    private String line13, line14, line15, line15Title, line16, line17a, line17b, line17c;
    // Part III
    private String line18, line19a, line19b, line19c;
    private String line20Type;
    private String line21a, line21b, line21cCode, line21cDate;
    private BigDecimal line22;
    private String line23a, line23b, line24a, line24b, line25;
    // Direct Deposit
    private String routingNumber, accountType, accountNumber;
    // Signature
    private String sigSignature, sigDate, sigNameTitle, taxpayerPin, sigFirstName, sigLastName;
    // Paid Preparer
    private String prepName, prepSignature, prepDate;
    private Boolean prepSelfEmployed;
    private String prepPtin, prepFirmName, prepFirmEin, prepPhone, prepFirmAddress;
    // Schedule A rows (populated for bond types 102, 103, 104, 105)
    private List<ScheduleARowResponse> scheduleARows;
    // IRS e-file XML (populated only after successful submission)
    private String generatedXml;
}
