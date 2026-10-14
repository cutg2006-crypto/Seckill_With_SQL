package com.hzr.seckillwithsql.controller;

import com.hzr.seckillwithsql.common.BaseResponse;
import com.hzr.seckillwithsql.common.ErrorCode;
import com.hzr.seckillwithsql.common.ResultUtils;
import com.hzr.seckillwithsql.exception.ThrowUtils;
import com.hzr.seckillwithsql.model.dto.ProductRequest;
import com.hzr.seckillwithsql.model.dto.UserSeckillRequest;
import com.hzr.seckillwithsql.model.entity.OrderData;
import com.hzr.seckillwithsql.model.entity.ProductData;

import com.hzr.seckillwithsql.model.vo.OrderResponse;
import com.hzr.seckillwithsql.model.vo.ProductResponse;
import com.hzr.seckillwithsql.service.OrderDataService;
import com.hzr.seckillwithsql.service.ProductDataService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Resource
    private OrderDataService orderDataService;
    @Resource
    private ProductDataService productDataService;

    @PostMapping("/setproduct")
    public BaseResponse<ProductResponse> setProduct(@Valid @RequestBody ProductRequest productRequest) {
        return ResultUtils.success(productDataService.setProduct(productRequest));
    }

    @GetMapping("/initseckill")
    public BaseResponse<ProductResponse> initSeckill(@Valid @RequestParam int productID) {
        return ResultUtils.success(productDataService.initSeckill(productID));
    }

    @PostMapping("/participate")
    public BaseResponse<OrderResponse> participateSeckill(@Valid @RequestBody UserSeckillRequest userSeckillRequest) {
        return ResultUtils.success(orderDataService.participateSeckill(userSeckillRequest));
    }

//    @GetMapping("/updatesql")
//    public BaseResponse<String> updateSql() {
//        boolean orderResult = orderDataService.updateSql();
//        boolean productResult = productDataService.updateSql();
//        if(orderResult && productResult) {
//            return ResultUtils.success("数据库更新成功，数据已持久化！");
//        }
//        return  ResultUtils.error("这是一个占位符错误，一般情况下不应该看见它！");
//    }

    @GetMapping("/updatesql")
    public BaseResponse<String> updateSql() {
        return ResultUtils.success(productDataService.updateSql()); // 订单信息会持续被写入数据库，因而不需要数据库更新
    }


}
