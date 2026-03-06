package com.gateway.controllers;

import com.gateway.dto.RefundResponse;
import com.gateway.entities.Merchant;
import com.gateway.services.MerchantService;
import com.gateway.services.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;
    private final MerchantService merchantService;

    @GetMapping("/{refundId}")
    public ResponseEntity<RefundResponse> getRefund(
            @PathVariable String refundId,
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret) {

        Merchant merchant = merchantService.authenticate(apiKey, apiSecret);
        if (merchant == null) {
            return ResponseEntity.status(401).build();
        }

        var refund = refundService.getRefundByRefundId(refundId);
        if (refund == null || !refund.getPayment().getOrder().getMerchant().getId().equals(merchant.getId())) {
            return ResponseEntity.notFound().build();
        }

        var response = refundService.toResponse(refund);
        return ResponseEntity.ok(response);
    }
}