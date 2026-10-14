package com.hzr.seckillwithsql.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ProductRequest {

    @Positive(message = "商品id需大于0")
    private int productID;

    @NotNull(message = "数量不能为空")
    private int quantity;
}
