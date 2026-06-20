package com.orderprocessing.dto;

import com.orderprocessing.entity.OrderStatus;
import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class CreateOrderResponse {
    Long id;
    OrderStatus status;
    BigDecimal totalAmount;
}

