package com.hzr.seckillwithsql.common;

public class ResultUtils {

    //成功
    public static<T> BaseResponse<T> success(T data){
        return new BaseResponse<>(200, data, "ok");
    }

    //失败（3个情况）
    public static<T> BaseResponse<T> error(T data){
        return new BaseResponse<>(666, data, "error");
    }

    public static BaseResponse<?> error(int code, String message){
        return new BaseResponse<>(code,null, message);
    }

    public static BaseResponse<?> error(ErrorCode errorCode, String message){
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }

    public static BaseResponse<?> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }
}
