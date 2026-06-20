package com.orderprocessing.controller;

import com.orderprocessing.dto.CreateOrderRequest;
import com.orderprocessing.dto.CreateOrderResponse;
import com.orderprocessing.dto.OrderResponse;
import com.orderprocessing.dto.ApiResponse;
import com.orderprocessing.entity.OrderStatus;
import com.orderprocessing.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
public class CustomerOrderController {

    private final OrderService orderService;

    public CustomerOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create a new customer order",
            description = "Creates an order in PENDING state and calculates totalAmount from all items.",
            tags = "Customer Orders"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created",
                    content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation error")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    {
                      "customerName": "John Doe",
                      "customerEmail": "john@example.com",
                      "items": [
                        {
                          "productId": "P100",
                          "productName": "Laptop",
                          "quantity": 1,
                          "unitPrice": 50000
                        },
                        {
                          "productId": "P200",
                          "productName": "Mouse",
                          "quantity": 2,
                          "unitPrice": 500
                        }
                      ]
                    }
                    """))
    )
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderResponse orderResponse = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order created successfully", orderResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", tags = "Customer Orders")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(@PathVariable Long id) {
        OrderResponse orderResponse = orderService.getOrderById(id);
        return ResponseEntity.ok(ApiResponse.success("Order retrieved successfully", orderResponse));
    }

    @GetMapping
    @Operation(summary = "List orders", description = "Returns all orders or filters by status.", tags = "Customer Orders")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Orders retrieved",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = """
                    [
                      {
                        "id": 1,
                        "customerName": "John Doe",
                        "customerEmail": "john@example.com",
                        "totalAmount": 51000,
                        "status": "PENDING",
                        "createdAt": "2026-06-20T10:00:00",
                        "updatedAt": "2026-06-20T10:00:00",
                        "items": []
                      }
                    ]
                    """)))
    public ResponseEntity<ApiResponse<List<OrderResponse>>> listOrders(@RequestParam(required = false) OrderStatus status) {
        List<OrderResponse> orders = orderService.listOrders(status);
        return ResponseEntity.ok(ApiResponse.success("Orders retrieved successfully", orders));
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancels order only if current status is PENDING.", tags = "Customer Orders")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order cancelled",
                    content = @Content(mediaType = "application/json")),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid order state")
    })
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id) {
        OrderResponse orderResponse = orderService.cancelOrder(id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled successfully", orderResponse));
    }
}



