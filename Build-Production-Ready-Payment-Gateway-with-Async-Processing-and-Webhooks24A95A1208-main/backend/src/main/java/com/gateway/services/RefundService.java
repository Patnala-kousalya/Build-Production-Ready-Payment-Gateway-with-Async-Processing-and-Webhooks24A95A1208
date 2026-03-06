package com.gateway.services;

import com.gateway.dto.RefundRequest;
import com.gateway.dto.RefundResponse;
import com.gateway.entities.Payment;
import com.gateway.entities.Refund;
import com.gateway.repositories.PaymentRepository;
import com.gateway.repositories.RefundRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefundService {

    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WebhookService webhookService;

    @Transactional
    public RefundResponse createRefund(String paymentId, RefundRequest request, Long merchantId) {
        Payment payment = paymentRepository.findByPaymentId(paymentId)
            .orElseThrow(() -> new RuntimeException("Payment not found"));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Can only refund successful payments");
        }

        BigDecimal totalRefunded = refundRepository.getTotalRefundedAmount(payment.getId());
        if (totalRefunded.add(request.getAmount()).compareTo(payment.getAmount()) > 0) {
            throw new RuntimeException("Refund amount exceeds payment amount");
        }

        Refund refund = new Refund();
        refund.setRefundId("ref_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        refund.setPayment(payment);
        refund.setAmount(request.getAmount());
        refund.setStatus(Refund.RefundStatus.PENDING);

        refund = refundRepository.save(refund);

        // Enqueue job for async processing
        redisTemplate.opsForList().leftPush("refund_queue", refund.getId());

        return toResponse(refund);
    }

    public Refund getRefundById(Long refundId) {
        return refundRepository.findById(refundId).orElse(null);
    }

    public Refund getRefundByRefundId(String refundId) {
        return refundRepository.findByRefundId(refundId).orElse(null);
    }

    public void updateRefundStatus(Long refundId, Refund.RefundStatus status) {
        Refund refund = refundRepository.findById(refundId).orElse(null);
        if (refund != null) {
            refund.setStatus(status);
            refund.setProcessedAt(java.time.LocalDateTime.now());
            refundRepository.save(refund);

            // Trigger webhook
            if (status == Refund.RefundStatus.PROCESSED) {
                webhookService.sendWebhook(refund.getPayment().getOrder().getMerchant(), "refund.processed", refund);
            }
        }
    }

    public RefundResponse toResponse(Refund refund) {
        RefundResponse response = new RefundResponse();
        response.setRefundId(refund.getRefundId());
        response.setPaymentId(refund.getPayment().getPaymentId());
        response.setAmount(refund.getAmount());
        response.setStatus(refund.getStatus().name());
        response.setCreatedAt(refund.getCreatedAt());
        response.setProcessedAt(refund.getProcessedAt());
        return response;
    }
}