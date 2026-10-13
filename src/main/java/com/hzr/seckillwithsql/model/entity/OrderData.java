package com.hzr.seckillwithsql.model.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.Data;

/**
 * 订单信息
 * @TableName order_data
 */
@TableName(value ="order_data")
@Data
public class OrderData {
    /**
     * 用户id
     */

    private Integer userId;

    /**
     * 商品id
     */

    private Integer productId;

    /**
     * 创建时间
     */
    private Date createTime;
}
