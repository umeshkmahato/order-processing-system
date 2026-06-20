package com.orderprocessing.scheduler;

import com.orderprocessing.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderScheduler {

    private static final Logger log = LoggerFactory.getLogger(OrderScheduler.class);

    private final OrderService orderService;

    public OrderScheduler(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(cron = "${app.scheduler.pending-to-processing-cron:0 */5 * * * *}")
    public void movePendingOrdersToProcessing() {
        int updatedCount = orderService.processPendingOrders();
        log.info("Scheduled run completed, updatedCount={}", updatedCount);
    }
}

