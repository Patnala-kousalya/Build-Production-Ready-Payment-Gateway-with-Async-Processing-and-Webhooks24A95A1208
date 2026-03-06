package com.gateway.services;

import com.gateway.dto.OrderRequest;
import com.gateway.dto.OrderResponse;
import com.gateway.entities.Merchant;
import com.gateway.entities.Order;
import com.gateway.repositories.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public Order createOrder(OrderRequest request, Merchant merchant) {
        Order order = new Order();
        order.setOrderId(request.getOrderId());
        order.setMerchant(merchant);
        order.setAmount(request.getAmount());
        order.setCurrency(request.getCurrency());
        order.setCustomerEmail(request.getCustomerEmail());
        return orderRepository.save(order);
    }

    public Order getOrderByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId).orElse(null);
    }

    public OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setOrderId(order.getOrderId());
        response.setAmount(order.getAmount());
        response.setCurrency(order.getCurrency());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setCreatedAt(order.getCreatedAt());
        return response;
    }
}