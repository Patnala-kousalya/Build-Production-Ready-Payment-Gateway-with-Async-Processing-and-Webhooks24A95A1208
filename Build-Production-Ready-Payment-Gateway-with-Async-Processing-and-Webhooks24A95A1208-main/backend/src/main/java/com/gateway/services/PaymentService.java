package com.gateway.services;

import com.gateway.dto.PaymentRequest;
import com.gateway.dto.PaymentResponse;
import com.gateway.entities.*;
import com.gateway.repositories.IdempotencyKeyRepository;
import com.gateway.repositories.OrderRepository;
import com.gateway.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WebhookService webhookService;

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request, Merchant merchant, String idempotencyKey) {
        // Check idempotency
        if (idempotencyKey != null) {
            IdempotencyKey existingKey = idempotencyKeyRepository
                    .findByKeyAndMerchantId(idempotencyKey, merchant.getId())
                    .orElse(null);
            if (existingKey != null && existingKey.getExpiresAt().isAfter(LocalDateTime.now())) {
                // Return cached response
                return parseCachedResponse(existingKey.getResponse());
            }
        }

        Order order = orderRepository.findByOrderId(request.getOrderId())
                .filter(o -> o.getMerchant().getId().equals(merchant.getId()))
                .orElseThrow(() -> new RuntimeException("Order not found"));

        Payment payment = new Payment();
        payment.setPaymentId("pay_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        payment.setOrder(order);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setMethod(Payment.PaymentMethod.valueOf(request.getMethod().name()));
        payment.setStatus(Payment.PaymentStatus.PENDING);

        payment = paymentRepository.save(payment);

        // Enqueue job for async processing
        redisTemplate.opsForList().leftPush("payment_queue", payment.getId());

        PaymentResponse response = toResponse(payment);

        // Cache response for idempotency
        if (idempotencyKey != null) {
            IdempotencyKey key = new IdempotencyKey();
            key.setKey(idempotencyKey);
            key.setMerchant(merchant);
            key.setResponse(toJson(response));
            idempotencyKeyRepository.save(key);
        }

        return response;
    }

    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId).orElse(null);
    }

    public Payment getPaymentByPaymentId(String paymentId) {
        return paymentRepository.findByPaymentId(paymentId).orElse(null);
    }

    public void updatePaymentStatus(Long paymentId, Payment.PaymentStatus status) {
        Payment payment = paymentRepository.findById(paymentId).orElse(null);
        if (payment != null) {
            payment.setStatus(status);
            payment.setProcessedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            // Trigger webhook
            if (status == Payment.PaymentStatus.SUCCESS) {
                webhookService.sendWebhook(payment.getOrder().getMerchant(), "payment.success", payment);
            } else if (status == Payment.PaymentStatus.FAILED) {
                webhookService.sendWebhook(payment.getOrder().getMerchant(), "payment.failed", payment);
            }
        }
    }

    public PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setPaymentId(payment.getPaymentId());
        response.setOrderId(payment.getOrder().getOrderId());
        response.setAmount(payment.getAmount());
        response.setCurrency(payment.getCurrency());
        response.setMethod(payment.getMethod().name());
        response.setStatus(payment.getStatus().name());
        response.setCreatedAt(payment.getCreatedAt());
        response.setProcessedAt(payment.getProcessedAt());
        return response;
    }

    private PaymentResponse parseCachedResponse(String json) {
        // Simple parsing - in real app use Jackson
        return new PaymentResponse(); // Placeholder
    }

    private String toJson(PaymentResponse response) {
        // Simple serialization - in real app use Jackson
        return "{}"; // Placeholder
    }
}