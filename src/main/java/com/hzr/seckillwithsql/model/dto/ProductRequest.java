package com.hzr.seckillwithsql.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProductRequest {

    @NotBlank(message = "商品id不能为空")
    private int productId;

    @NotBlank(message = "数量不能为空")
    private int quantity;
}
