package com.orderprocessing.scheduler;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.orderprocessing.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderSchedulerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderScheduler orderScheduler;

    @Test
    void movePendingOrdersToProcessing_shouldInvokeService() {
        when(orderService.processPendingOrders()).thenReturn(2);

        orderScheduler.movePendingOrdersToProcessing();

        verify(orderService).processPendingOrders();
    }
}

