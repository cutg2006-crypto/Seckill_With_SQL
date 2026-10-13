package com.hzr.seckillwithsql.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzr.seckillwithsql.common.ErrorCode;
import com.hzr.seckillwithsql.exception.ThrowUtils;
import com.hzr.seckillwithsql.model.dto.ProductRequest;
import com.hzr.seckillwithsql.model.entity.ProductData;
import com.hzr.seckillwithsql.model.vo.ProductResponse;
import com.hzr.seckillwithsql.service.ProductDataService;
import com.hzr.seckillwithsql.mapper.ProductDataMapper;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

/**
 * @author Administrator
 * @description 针对表【product_data(商品信息)】的数据库操作Service实现
 * @createDate 2026-10-13 21:16:51
 */
@Service
public class ProductDataServiceImpl extends ServiceImpl<ProductDataMapper, ProductData>
        implements ProductDataService{

    @Resource
    private StringRedisTemplate redisTemplate;

    @Override
    public ProductResponse setProduct(ProductRequest productRequest) {
        int productID = productRequest.getProductId();
        int quantity = productRequest.getQuantity();

        ThrowUtils.throwIf(productID < 0 , ErrorCode.PARAMS_ERROR);

        QueryWrapper<ProductData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productID);
        ProductData existing = this.getOne(queryWrapper);

        if(existing == null){
            ThrowUtils.throwIf(quantity < 0 , ErrorCode.PARAMS_ERROR);
            ProductData productData = new ProductData();
            productData.setProductId(productID);
            productData.setQuantity(quantity);
            this.save(productData);
            return new ProductResponse(productID, quantity, "新增商品信息成功！");
        }

        existing.setQuantity(existing.getQuantity() + quantity);
        this.save(existing);
        return new ProductResponse(productID, quantity, "更新商品信息成功！");

    }

    @Override
    public ProductResponse initSeckill(int productID) {
        ThrowUtils.throwIf(productID < 0 , ErrorCode.PARAMS_ERROR);
        QueryWrapper<ProductData> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("product_id", productID);
        ProductData existing = this.getOne(queryWrapper);

        ThrowUtils.throwIf(existing == null, ErrorCode.FAILED_TO_INIT);
        String quantity = String.valueOf(existing.getQuantity());
        redisTemplate.opsForValue().set("seckill:product:" + productID, quantity);
        redisTemplate.opsForSet().add("seckill:product", String.valueOf(productID));

        return new ProductResponse(productID, existing.getQuantity(), "商品秒杀活动初始化成功！");
    }



}
