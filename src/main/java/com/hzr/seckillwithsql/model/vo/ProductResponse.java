package com.hzr.seckillwithsql.model.vo;

import lombok.Data;

@Data
public class ProductResponse {

    private int productID;

    private int quantity;

    private String message;

    public ProductResponse(int productID, int quantity, String message) {
        this.productID = productID;
        this.quantity = quantity;
        this.message = message;
    }


}
