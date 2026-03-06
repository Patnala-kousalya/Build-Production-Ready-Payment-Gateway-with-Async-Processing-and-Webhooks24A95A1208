package com.gateway.controllers;

import com.gateway.dto.PaymentRequest;
import com.gateway.dto.PaymentResponse;
import com.gateway.dto.RefundRequest;
import com.gateway.dto.RefundResponse;
import com.gateway.entities.Merchant;
import com.gateway.services.MerchantService;
import com.gateway.services.PaymentService;
import com.gateway.services.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RefundService refundService;
    private final MerchantService merchantService;

    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestBody PaymentRequest request,
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) {

        Merchant merchant = merchantService.authenticate(apiKey, apiSecret);
        if (merchant == null) {
            return ResponseEntity.status(401).build();
        }

        var response = paymentService.createPayment(request, merchant, idempotencyKey);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/{paymentId}/refunds")
    public ResponseEntity<RefundResponse> createRefund(
            @PathVariable String paymentId,
            @RequestBody RefundRequest request,
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret) {

        Merchant merchant = merchantService.authenticate(apiKey, apiSecret);
        if (merchant == null) {
            return ResponseEntity.status(401).build();
        }

        var response = refundService.createRefund(paymentId, request, merchant.getId());
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable String paymentId,
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret) {

        Merchant merchant = merchantService.authenticate(apiKey, apiSecret);
        if (merchant == null) {
            return ResponseEntity.status(401).build();
        }

        var payment = paymentService.getPaymentByPaymentId(paymentId);
        if (payment == null || !payment.getOrder().getMerchant().getId().equals(merchant.getId())) {
            return ResponseEntity.notFound().build();
        }

        var response = paymentService.toResponse(payment);
        return ResponseEntity.ok(response);
    }
}