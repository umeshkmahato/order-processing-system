package com.orderprocessing.dto;

import com.orderprocessing.entity.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusRequest {

    @NotNull(message = "status is required")
    private OrderStatus status;
}

