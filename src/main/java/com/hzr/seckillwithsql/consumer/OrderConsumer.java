package com.hzr.seckillwithsql.consumer;

import com.alibaba.fastjson2.JSON;
import com.hzr.seckillwithsql.model.entity.OrderData;
import com.hzr.seckillwithsql.service.OrderDataService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OrderConsumer {
    private static final String ORDER_QUEUE = "seckill:orders";

    private final StringRedisTemplate redisTemplate ;

    private final OrderDataService orderDataService;
    private volatile boolean running = true;
    private Thread worker;

    public OrderConsumer(StringRedisTemplate redisTemplate, OrderDataService orderDataService) {
        this.redisTemplate = redisTemplate;
        this.orderDataService = orderDataService;
    }

    @PostConstruct
    public void start() {
        worker = new Thread(this::consumeLoop, "order-consumer");
        worker.setDaemon(true);
        worker.start();
    }

    private void consumeLoop() {
        log.info("订单消费者启动...");
        while (running) {
            try {
                String json = redisTemplate.opsForList()
                        .rightPop(ORDER_QUEUE, 3, TimeUnit.SECONDS);
                if (json != null) {
                    OrderData order = JSON.parseObject(json, OrderData.class);
                    orderDataService.processOrder(order);   // 核心：委托给 Service
                }
            } catch (Exception e) {
                log.error("消费订单异常", e);
                sleepQuietly(1_000); //退避 1 秒，很重要
            }
        }
        log.info("订单消费者退出");
    }

    private void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PreDestroy
    public void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
    }





}
