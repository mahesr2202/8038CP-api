package com.example.form8038cp.payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payments", description = "Stripe payment intent management")
public class PaymentController {

    public PaymentController(@Value("${app.stripe.secret-key}") String stripeKey) {
        Stripe.apiKey = stripeKey;
    }

    @Operation(summary = "Create a Stripe PaymentIntent for the $50 submission fee")
    @PostMapping("/create-payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent() {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(5000L)
                    .setCurrency("usd")
                    .setDescription("Form 8038-CP Submission Fee")
                    .setAutomaticPaymentMethods(
                            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                    .setEnabled(true)
                                    .build())
                    .build();
            PaymentIntent intent = PaymentIntent.create(params);
            return ResponseEntity.ok(Map.of("clientSecret", intent.getClientSecret()));
        } catch (StripeException e) {
            log.error("Failed to create PaymentIntent: {}", e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Payment initialization failed. Please try again."));
        }
    }
}
