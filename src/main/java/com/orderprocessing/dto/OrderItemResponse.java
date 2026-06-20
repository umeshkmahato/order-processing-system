package com.orderprocessing.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrderItemResponse {
    Long id;
    String productId;
    String productName;
    Integer quantity;
    BigDecimal unitPrice;
}

