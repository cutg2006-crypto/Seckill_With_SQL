package com.hzr.seckillwithsql.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hzr.seckillwithsql.common.ErrorCode;
import com.hzr.seckillwithsql.constant.UserConstant;
import com.hzr.seckillwithsql.exception.ThrowUtils;
import com.hzr.seckillwithsql.model.dto.ProductRequest;
import com.hzr.seckillwithsql.model.entity.ProductData;
import com.hzr.seckillwithsql.model.vo.ProductResponse;
import com.hzr.seckillwithsql.service.ProductDataService;
import com.hzr.seckillwithsql.mapper.ProductDataMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * @author Administrator
 * @description 针对表【product_data(商品信息)】的数据库操作Service实现
 * @createDate 2026-10-13 21:16:51
 */
@Service
@Slf4j
public class ProductDataServiceImpl extends ServiceImpl<ProductDataMapper, ProductData>
        implements ProductDataService{

    // @Resource 默认按【字段名】找 bean，字段名叫 redisTemplate 会命中 RedisTemplate<Object,Object>，
    // 因此这里显式指定 StringRedisTemplate 对应的 bean 名
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;

    @Override
    public ProductResponse setProduct(ProductRequest productRequest) {
        int productID = productRequest.getProductID();
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
        this.updateById(existing);
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

        // 添加商品库存到秒杀活动
        redisTemplate.opsForValue().set(UserConstant.productStockKey(productID), quantity);

        // 添加商品至秒杀活动集合
        redisTemplate.opsForSet().add(UserConstant.ACTIVE_PRODUCT_KEY, String.valueOf(productID));

        // 重置该商品的已购用户集合：否则重新初始化库存后复测，
        // 上一次下过单的用户会被误判为重复购买（70002）
        redisTemplate.delete(UserConstant.boughtUserKey(productID));

        return new ProductResponse(productID, existing.getQuantity(), "商品秒杀活动初始化成功！");
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateSql() {
        // 1. 取出已开启秒杀活动的全部商品 ID（Set 成员）
        Set<String> productIds = redisTemplate.opsForSet().members(UserConstant.ACTIVE_PRODUCT_KEY);

        if (productIds == null || productIds.isEmpty()) {
            log.info("秒杀活动商品集合 {} 为空，无库存需要同步", UserConstant.ACTIVE_PRODUCT_KEY);
            return "秒杀活动商品集合为空，无库存需要更新！";
        }

        // 2. 遍历商品 ID，逐个读取 Redis 中的剩余库存，并写回数据库
        int successCount = 0;
        for (String productId : productIds) {

            String stockStr = redisTemplate.opsForValue().get(UserConstant.productStockKey(productId));

            if (stockStr == null) {
                log.warn("商品 {} 在 Redis 中没有库存记录，跳过同步", productId);
                continue;
            }

            int dbProductId;
            int stock;
            try {
                dbProductId = Integer.parseInt(productId);
                stock = Integer.parseInt(stockStr);
            } catch (NumberFormatException e) {
                log.error("商品 {} 的库存值 {} 非法，跳过同步", productId, stockStr, e);
                continue;
            }

            // 防御：库存扣减后若回滚失败，Redis 中可能残留负数，落库时按 0 处理
            stock = Math.max(stock, 0);

            // 3. 按主键 product_id 更新 quantity 字段（只生成一条 UPDATE 语句）
            ProductData productData = new ProductData();
            productData.setProductId(dbProductId);
            productData.setQuantity(stock);

            if (this.updateById(productData)) {
                successCount++;
                log.info("商品 {} 的库存已同步到数据库：{}", productId, stock);
            } else {
                log.warn("商品 {} 在表 product_data 中不存在，未更新任何数据", productId);
            }
        }

        log.info("库存同步完成：活动商品共 {} 个，成功更新 {} 个", productIds.size(), successCount);
        return "商品信息成功更新至数据库中！";
    }


}
