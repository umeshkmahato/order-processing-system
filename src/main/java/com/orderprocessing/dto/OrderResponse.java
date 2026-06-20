package com.orderprocessing.dto;

import com.orderprocessing.entity.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderResponse {
    Long id;
    String customerName;
    String customerEmail;
    BigDecimal totalAmount;
    OrderStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<OrderItemResponse> items;
}

