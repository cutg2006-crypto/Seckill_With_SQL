package com.hzr.seckillwithsql.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class OrderResponse {

    private int userID;

    private int productID;

    private String message;



}
