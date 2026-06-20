package com.orderprocessing.service.impl;

import com.orderprocessing.dto.CreateOrderItemRequest;
import com.orderprocessing.dto.CreateOrderRequest;
import com.orderprocessing.dto.CreateOrderResponse;
import com.orderprocessing.dto.OrderResponse;
import com.orderprocessing.entity.Order;
import com.orderprocessing.entity.OrderItem;
import com.orderprocessing.entity.OrderStatus;
import com.orderprocessing.exception.InvalidOrderStateException;
import com.orderprocessing.exception.OrderNotFoundException;
import com.orderprocessing.mapper.OrderMapper;
import com.orderprocessing.repository.OrderRepository;
import com.orderprocessing.service.OrderService;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request) {
        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setStatus(OrderStatus.PENDING);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemRequest.getProductId());
            item.setProductName(itemRequest.getProductName());
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitPrice(itemRequest.getUnitPrice());
            order.addItem(item);

            BigDecimal itemTotal = itemRequest.getUnitPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);
        }
        order.setTotalAmount(totalAmount);

        Order saved = orderRepository.save(order);
        log.info("Created order id={} with totalAmount={}", saved.getId(), saved.getTotalAmount());
        return orderMapper.toCreateResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long id) {
        Order order = findOrder(id);
        return orderMapper.toOrderResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> listOrders(OrderStatus status) {
        List<Order> orders = (status == null)
                ? orderRepository.findAll()
                : orderRepository.findByStatus(status);

        return orders.stream().map(orderMapper::toOrderResponse).toList();
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order order = findOrder(id);
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        log.info("Updated order id={} status to {}", saved.getId(), saved.getStatus());
        return orderMapper.toOrderResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(Long id) {
        Order order = findOrder(id);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new InvalidOrderStateException("Only PENDING orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        log.info("Cancelled order id={}", saved.getId());
        return orderMapper.toOrderResponse(saved);
    }

    @Override
    @Transactional
    public int processPendingOrders() {
        List<Order> pendingOrders = orderRepository.findByStatus(OrderStatus.PENDING);
        pendingOrders.forEach(order -> order.setStatus(OrderStatus.PROCESSING));
        orderRepository.saveAll(pendingOrders);
        log.info("Scheduler updated {} orders from PENDING to PROCESSING", pendingOrders.size());
        return pendingOrders.size();
    }

    private Order findOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }
}

