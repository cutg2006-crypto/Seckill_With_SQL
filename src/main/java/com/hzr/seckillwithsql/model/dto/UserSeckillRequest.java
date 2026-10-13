package com.hzr.seckillwithsql.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class UserSeckillRequest {

    @NotBlank(message = "用户id不能为空")
    private String userId;

    @NotBlank(message = "商品id不能为空")
    private String productId;


}
