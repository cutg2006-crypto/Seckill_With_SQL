package com.hzr.seckillwithsql.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UserSeckillRequest {

    @Positive(message = "用户id不能小于0")
    private String userId;

    @NotNull(message = "商品id不能为空")
    private String productId;


}
