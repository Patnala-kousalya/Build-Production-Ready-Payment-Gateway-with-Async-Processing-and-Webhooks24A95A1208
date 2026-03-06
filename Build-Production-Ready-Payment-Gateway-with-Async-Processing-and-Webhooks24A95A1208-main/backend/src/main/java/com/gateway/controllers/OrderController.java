package com.gateway.controllers;

import com.gateway.dto.OrderRequest;
import com.gateway.dto.OrderResponse;
import com.gateway.entities.Merchant;
import com.gateway.services.MerchantService;
import com.gateway.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final MerchantService merchantService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestBody OrderRequest request,
            @RequestHeader("X-Api-Key") String apiKey,
            @RequestHeader("X-Api-Secret") String apiSecret) {

        Merchant merchant = merchantService.authenticate(apiKey, apiSecret);
        if (merchant == null) {
            return ResponseEntity.status(401).build();
        }

        var order = orderService.createOrder(request, merchant);
        var response = orderService.toResponse(order);
        return ResponseEntity.status(201).body(response);
    }
}