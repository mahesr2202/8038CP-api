package com.example.form8038cp.form.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SubmitRequest {
    private String stripePaymentIntentId;
    /** Resolved from the HTTP request by the controller — not sent by the client. */
    private String clientIp;
}
