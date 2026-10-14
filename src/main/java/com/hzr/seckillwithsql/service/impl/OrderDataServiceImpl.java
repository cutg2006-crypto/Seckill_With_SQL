package com.hzr.seckillwithsql.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzr.seckillwithsql.common.ErrorCode;
import com.hzr.seckillwithsql.constant.UserConstant;
import com.hzr.seckillwithsql.exception.ThrowUtils;
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

    // @Resource 默认按【字段名】找 bean，字段名叫 redisTemplate 会命中 RedisTemplate<Object,Object>，
    // 因此这里显式指定 StringRedisTemplate 对应的 bean 名
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Override
    public OrderResponse participateSeckill(UserSeckillRequest userSeckillRequest) {
        String userID = userSeckillRequest.getUserId();
        String productID = userSeckillRequest.getProductId();

        Boolean pass = redisTemplate.opsForValue().setIfAbsent(
                UserConstant.rateLimitKey(userID),
                UserConstant.RATE_LIMIT_VALUE,
                UserConstant.RATE_LIMIT_SECONDS, TimeUnit.SECONDS);

        // 频繁购买
        ThrowUtils.throwIf(!Boolean.TRUE.equals(pass), ErrorCode.TOO_OFTEN_ERROR);

        // 重复购买（一人一单：按商品维度记录已购用户）
        ThrowUtils.throwIf(Boolean.TRUE.equals(redisTemplate.opsForSet()
                .isMember(UserConstant.boughtUserKey(productID), userID)), ErrorCode.REPEATED_ERROR );

        // 如果商品未开启秒杀活动
        ThrowUtils.throwIf(!Boolean.TRUE.equals(redisTemplate.opsForSet()
                .isMember(UserConstant.ACTIVE_PRODUCT_KEY, productID)), ErrorCode.USER_NO_SUCH_PRODUCT);

        // 正常购买
        Long stock = redisTemplate.opsForValue().decrement(UserConstant.productStockKey(productID));
        if(stock < 0){
            redisTemplate.opsForValue().increment(UserConstant.productStockKey(productID));
            redisTemplate.delete(UserConstant.rateLimitKey(userID));

            ThrowUtils.throwIf(Boolean.TRUE, ErrorCode.SOLD_OUT);
        }

        // 加入订单集合

        OrderData order = new OrderData();
        order.setUserId(Integer.parseInt(userID));
        order.setProductId(Integer.parseInt(productID));
        order.setCreateTime(new Date());

        redisTemplate.opsForSet().add(UserConstant.boughtUserKey(productID), userID); // 将用户加入商品对应购买集合，防止重复购买

        redisTemplate.opsForList().leftPush(UserConstant.ORDER_QUEUE_KEY, JSON.toJSONString(order));

        OrderResponse orderResponse = new OrderResponse(Integer.parseInt(userID), Integer.parseInt(productID),"秒杀成功，订单处理中...");
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
        orderData.setProductId(order.getProductId());
        orderData.setCreateTime(now);

        if(this.save(orderData)){
            log.info("订单处理完成 - 用户ID: {}, 商品ID： {}", order.getUserId(), order.getProductId());
        } else {
            log.info("订单处理发生异常，用户ID: {}, 商品ID： {}", order.getUserId(), order.getProductId());
        }

    }
}