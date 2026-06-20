package com.orderprocessing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.orderprocessing.dto.CreateOrderItemRequest;
import com.orderprocessing.dto.CreateOrderRequest;
import com.orderprocessing.dto.CreateOrderResponse;
import com.orderprocessing.dto.OrderResponse;
import com.orderprocessing.entity.Order;
import com.orderprocessing.entity.OrderItem;
import com.orderprocessing.entity.OrderStatus;
import com.orderprocessing.exception.InvalidOrderStateException;
import com.orderprocessing.mapper.OrderMapper;
import com.orderprocessing.repository.OrderRepository;
import com.orderprocessing.service.impl.OrderServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(1L);
        order.setCustomerName("John Doe");
        order.setCustomerEmail("john@example.com");
        order.setStatus(OrderStatus.PENDING);
        order.setTotalAmount(BigDecimal.valueOf(51000));

        OrderItem item = new OrderItem();
        item.setProductId("P100");
        item.setProductName("Laptop");
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.valueOf(51000));
        order.addItem(item);
    }

    @Test
    void createOrder_shouldPersistWithPendingStatusAndTotal() {
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("John Doe");
        request.setCustomerEmail("john@example.com");

        CreateOrderItemRequest item = new CreateOrderItemRequest();
        item.setProductId("P100");
        item.setProductName("Laptop");
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.valueOf(51000));
        request.setItems(List.of(item));

        CreateOrderResponse expected = CreateOrderResponse.builder()
                .id(1L)
                .status(OrderStatus.PENDING)
                .totalAmount(BigDecimal.valueOf(51000))
                .build();

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });
        when(orderMapper.toCreateResponse(any(Order.class))).thenReturn(expected);

        CreateOrderResponse response = orderService.createOrder(request);

        assertEquals(OrderStatus.PENDING, response.getStatus());
        assertEquals(BigDecimal.valueOf(51000), response.getTotalAmount());
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void getOrder_shouldReturnOrderDetails() {
        OrderResponse expected = OrderResponse.builder().id(1L).status(OrderStatus.PENDING).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderMapper.toOrderResponse(order)).thenReturn(expected);

        OrderResponse response = orderService.getOrderById(1L);

        assertEquals(1L, response.getId());
        verify(orderRepository).findById(1L);
    }

    @Test
    void cancelOrder_shouldSucceedWhenPending() {
        OrderResponse expected = OrderResponse.builder().id(1L).status(OrderStatus.CANCELLED).build();
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(expected);

        OrderResponse response = orderService.cancelOrder(1L);

        assertEquals(OrderStatus.CANCELLED, response.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_shouldFailWhenNotPending() {
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder(1L));
    }

    @Test
    void updateStatus_shouldSetProvidedStatus() {
        order.setStatus(OrderStatus.SHIPPED);
        OrderResponse expected = OrderResponse.builder().id(1L).status(OrderStatus.SHIPPED).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderResponse(order)).thenReturn(expected);

        OrderResponse response = orderService.updateStatus(1L, OrderStatus.SHIPPED);

        assertEquals(OrderStatus.SHIPPED, response.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void processPendingOrders_shouldMoveToProcessing() {
        Order pending = new Order();
        pending.setStatus(OrderStatus.PENDING);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(List.of(pending));

        int result = orderService.processPendingOrders();

        assertEquals(1, result);
        assertEquals(OrderStatus.PROCESSING, pending.getStatus());
        verify(orderRepository).saveAll(List.of(pending));
    }
}

