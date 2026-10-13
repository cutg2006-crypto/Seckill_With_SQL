package com.hzr.seckillwithsql.model.vo;

import lombok.Data;

import java.util.Date;

@Data
public class OrderResponse {

    private int userID;

    private int productID;

    private Date createTime;

    private String message;

}
