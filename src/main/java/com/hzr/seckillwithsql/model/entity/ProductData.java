package com.hzr.seckillwithsql.model.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商品信息
 * @TableName product_data
 */
@TableName(value ="product_data")
@Data
public class ProductData {
    /**
     * 商品id
     */
    @TableId
    private Integer productId;


    /**
     * 商品数量
     */
    private Integer quantity;
}
