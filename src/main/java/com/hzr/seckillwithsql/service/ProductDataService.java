package com.hzr.seckillwithsql.service;

import com.hzr.seckillwithsql.model.dto.ProductRequest;
import com.hzr.seckillwithsql.model.entity.ProductData;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hzr.seckillwithsql.model.vo.ProductResponse;
import jakarta.validation.Valid;

/**
 * @author Administrator
 * @description 针对表【product_data(商品信息)】的数据库操作Service
 * @createDate 2026-10-13 21:16:51
 */
public interface ProductDataService extends IService<ProductData> {

    ProductResponse setProduct(@Valid ProductRequest productRequest);

    ProductResponse initSeckill(@Valid int productID);

    String updateSql();
}
