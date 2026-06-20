package com.orderprocessing.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.orderprocessing.entity.Order;
import com.orderprocessing.entity.OrderItem;
import com.orderprocessing.entity.OrderStatus;
import com.orderprocessing.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerAdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void cleanDb() {
        orderRepository.deleteAll();
    }

    @Test
    void createOrder_shouldReturnCreated() throws Exception {
        String payload = """
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
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order created successfully"))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.totalAmount").value(51000))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void getOrder_shouldReturnOrderDetails() throws Exception {
        Order savedOrder = persistPendingOrder();

        mockMvc.perform(get("/api/orders/{id}", savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order retrieved successfully"))
                .andExpect(jsonPath("$.data.id").value(savedOrder.getId()))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
                .andExpect(jsonPath("$.data.items", hasSize(1)))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void listOrders_byStatus_shouldFilter() throws Exception {
        persistPendingOrder();

        Order shipped = persistPendingOrder();
        shipped.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(shipped);

        mockMvc.perform(get("/api/orders").param("status", "PENDING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Orders retrieved successfully"))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].status").value("PENDING"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void updateStatus_shouldChangeStatus() throws Exception {
        Order savedOrder = persistPendingOrder();
        String payload = objectMapper.writeValueAsString(java.util.Map.of("status", "SHIPPED"));

        mockMvc.perform(put("/api/orders/{id}/status", savedOrder.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order status updated successfully"))
                .andExpect(jsonPath("$.data.status").value("SHIPPED"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void cancelOrder_shouldFailForNonPending() throws Exception {
        Order savedOrder = persistPendingOrder();
        savedOrder.setStatus(OrderStatus.PROCESSING);
        orderRepository.save(savedOrder);

        mockMvc.perform(post("/api/orders/{id}/cancel", savedOrder.getId()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Only PENDING orders can be cancelled"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void cancelOrder_shouldSucceedForPendingOrder() throws Exception {
        Order savedOrder = persistPendingOrder();

        mockMvc.perform(post("/api/orders/{id}/cancel", savedOrder.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Order cancelled successfully"))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createOrder_shouldReturnValidationError() throws Exception {
        String payload = """
                {
                  "customerName": "",
                  "customerEmail": "invalid-email",
                  "items": []
                }
                """;

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void customerApiDocs_shouldExposeCustomerEndpoints() throws Exception {
        mockMvc.perform(get("/api-docs/customer"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths['/api/orders']").exists())
                .andExpect(jsonPath("$.paths['/api/orders/{id}/status']").doesNotExist());
    }

    @Test
    void adminApiDocs_shouldExposeAdminEndpoints() throws Exception {
        mockMvc.perform(get("/api-docs/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").exists())
                .andExpect(jsonPath("$.paths['/api/orders/{id}/status']").exists());
    }

    private Order persistPendingOrder() {
        Order order = new Order();
        order.setCustomerName("Jane");
        order.setCustomerEmail("jane@example.com");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(100));

        OrderItem item = new OrderItem();
        item.setProductId("P1");
        item.setProductName("Book");
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.valueOf(100));
        order.addItem(item);

        return orderRepository.save(order);
    }
}
