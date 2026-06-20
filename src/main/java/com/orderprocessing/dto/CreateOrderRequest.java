package com.orderprocessing.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

    @NotBlank(message = "customerName cannot be blank")
    private String customerName;

    @NotBlank(message = "customerEmail cannot be blank")
    @Email(message = "customerEmail must be a valid email")
    private String customerEmail;

    @NotEmpty(message = "items cannot be empty")
    @Valid
    private List<CreateOrderItemRequest> items;
}

