package com.hzr.seckillwithsql.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzr.seckillwithsql.common.ErrorCode;
import com.hzr.seckillwithsql.exception.ThrowUtils;
import com.hzr.seckillwithsql.model.dto.ProductRequest;
import com.hzr.seckillwithsql.model.dto.UserSeckillRequest;
import com.hzr.seckillwithsql.model.entity.OrderData;
import com.hzr.seckillwithsql.model.vo.OrderResponse;
import com.hzr.seckillwithsql.service.OrderDataService;
import com.hzr.seckillwithsql.mapper.OrderDataMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @description 针对表【order_data(订单信息)】的数据库操作Service实现
 * @createDate 2026-10-13 21:16:44
 */
@Service
@Slf4j
public class OrderDataServiceImpl extends ServiceImpl<OrderDataMapper, OrderData>
        implements OrderDataService{

    private static final String STOCK_KEY   = "seckill:product:";

    private static final String ORDER_QUEUE = "seckill:orders";
    private static final String BOUGHT_SET  = "seckill:product:users:";   // 一人一单：已购用户集合
    private static final String RATE_PREFIX = "seckill:product:rate:";   // 防抖：5 秒一次

    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public OrderResponse participateSeckill(UserSeckillRequest userSeckillRequest) {
        String userID = userSeckillRequest.getUserId();
        String productID = userSeckillRequest.getProductId();

        Boolean pass = redisTemplate.opsForValue().setIfAbsent(RATE_PREFIX + userID, "1", 5, TimeUnit.SECONDS);

        // 频繁购买
        ThrowUtils.throwIf(!Boolean.TRUE.equals(pass), ErrorCode.TOO_OFTEN_ERROR);

        // 重复购买
        ThrowUtils.throwIf(Boolean.TRUE.equals(redisTemplate.opsForSet()
                .isMember(BOUGHT_SET, String.valueOf(userID))), ErrorCode.REPEATED_ERROR );

        // 如果商品未开启秒杀活动
        ThrowUtils.throwIf(!Boolean.TRUE.equals(redisTemplate.opsForSet()
                .isMember(STOCK_KEY, String.valueOf(userID))), ErrorCode.USER_NO_SUCH_PRODUCT);

        // 正常购买
        Long stock = redisTemplate.opsForValue().decrement(STOCK_KEY + productID);
        if(stock < 0){
            redisTemplate.opsForValue().increment(STOCK_KEY + productID);
            redisTemplate.delete(RATE_PREFIX + userID);

            ThrowUtils.throwIf(Boolean.TRUE, ErrorCode.SOLD_OUT);
        }

        // 加入订单集合

        OrderData order = new OrderData();
        order.setUserId(Integer.parseInt(userID));
        order.setProductId(Integer.parseInt(productID));
        order.setCreateTime(new Date());

        redisTemplate.opsForList().leftPush(ORDER_QUEUE, JSON.toJSONString(order));

        OrderResponse  orderResponse = new OrderResponse(Integer.parseInt(userID), Integer.parseInt(productID),"秒杀成功，订单处理中...");
        return orderResponse;

    }

    @Override
    public void processOrder(OrderData order) {
        Date now = new Date();
        log.info("开始处理订单 - 用户ID: {}, 商品ID: {}, 创建时间: {}",
                order.getUserId(), order.getProductId(), now);

        //写入数据库
        QueryWrapper<OrderData> queryWrapper = new QueryWrapper<>();
        OrderData orderData = new OrderData();
        orderData.setUserId(order.getUserId());



    }
}