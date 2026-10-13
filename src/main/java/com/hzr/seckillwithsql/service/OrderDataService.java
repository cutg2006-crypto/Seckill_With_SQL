package com.hzr.seckillwithsql.service;

import com.hzr.seckillwithsql.model.dto.ProductRequest;
import com.hzr.seckillwithsql.model.dto.UserSeckillRequest;
import com.hzr.seckillwithsql.model.entity.OrderData;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hzr.seckillwithsql.model.vo.OrderResponse;
import jakarta.validation.Valid;

/**
 * @author Administrator
 * @description 针对表【order_data(订单信息)】的数据库操作Service
 * @createDate 2026-10-13 21:16:44
 */
public interface OrderDataService extends IService<OrderData> {

    OrderResponse participateSeckill(@Valid UserSeckillRequest userSeckillRequest);

    void processOrder(OrderData order);
}
