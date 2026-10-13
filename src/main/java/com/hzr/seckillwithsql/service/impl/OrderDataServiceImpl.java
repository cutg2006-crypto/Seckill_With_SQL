package com.hzr.seckillwithsql.service.impl;

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
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @description 针对表【order_data(订单信息)】的数据库操作Service实现
 * @createDate 2026-10-13 21:16:44
 */
@Service
public class OrderDataServiceImpl extends ServiceImpl<OrderDataMapper, OrderData>
        implements OrderDataService{

    private static final String STOCK_KEY   = "seckill:product:";
    private static final String STOCK_SET   = "seckill:product";
    private static final String ORDER_QUEUE = "seckill:orders";
    private static final String BOUGHT_SET  = "seckill:product:users:";   // 一人一单：已购用户集合
    private static final String RATE_PREFIX = "seckill:product:rate:";   // 防抖：5 秒一次

    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public OrderResponse participateSeckill(UserSeckillRequest userSeckillRequest) {
        String userID = userSeckillRequest.getUserId();
        String productID = userSeckillRequest.getProductId();

        Boolean pass = redisTemplate.opsForValue().setIfAbsent(RATE_PREFIX + userID, "1", 5, TimeUnit.SECONDS)

        ThrowUtils.throwIf(!Boolean.TRUE.equals(pass), ErrorCode.TOO_OFTEN_ERROR);

        ThrowUtils.throwIf(Boolean.TRUE.equals(redisTemplate.opsForSet()
                .isMember(BOUGHT_SET, String.valueOf(userID))), ErrorCode.REPEATED_ERROR );

        ThrowUtils.throwIf(!Boolean.TRUE.equals(redisTemplate.opsForSet()
                .isMember(STOCK_SET, String.valueOf(userID))), ErrorCode.USER_NO_SUCH_PRODUCT);



    }

    @Override
    public void processOrder(OrderData order) {

    }
}