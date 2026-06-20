package com.orderprocessing.service;

import com.orderprocessing.dto.CreateOrderRequest;
import com.orderprocessing.dto.CreateOrderResponse;
import com.orderprocessing.dto.OrderResponse;
import com.orderprocessing.entity.OrderStatus;
import java.util.List;

public interface OrderService {
    CreateOrderResponse createOrder(CreateOrderRequest request);

    OrderResponse getOrderById(Long id);

    List<OrderResponse> listOrders(OrderStatus status);

    OrderResponse updateStatus(Long id, OrderStatus status);

    OrderResponse cancelOrder(Long id);

    int processPendingOrders();
}

