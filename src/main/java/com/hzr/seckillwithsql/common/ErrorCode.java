package com.hzr.seckillwithsql.common;


public enum ErrorCode {

    // 用户相关 7000x：
    TOO_OFTEN_ERROR (70001, "您的操作过于频繁，请稍后再试"),
    REPEATED_ERROR (70002, "您已参与过本次秒杀，不能重复下单"),
    USER_NO_SUCH_PRODUCT (70003, "您想要购买的商品不存在或尚未开启秒杀活动"),

    // 商品相关 8000x
    FAILED_TO_INIT (80001, "商品秒杀活动初始化失败"),
    SOLD_OUT (80002, "商品已售罄，感谢参与"),

    //系统内部 6000x:
    SYSTEM_ERROR(60001,"系统内部错误" ),

    // 通用错误 4000x
    PARAMS_ERROR(40001,"请求参数错误" );

    private final int code;
    private final String message;
    ErrorCode(int code, String message){
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
