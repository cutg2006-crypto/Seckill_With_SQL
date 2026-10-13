package com.hzr.seckillwithsql.exception;

import com.hzr.seckillwithsql.common.ErrorCode;

public class BusinessException extends RuntimeException{

    private final int code;

    public BusinessException(int code, String message){
        super(message);
        this.code = code;
    }

    public BusinessException(ErrorCode errorCode){
        this(errorCode.getCode(), errorCode.getMessage());
    }

    public BusinessException(ErrorCode errorCode, String message){
        this(errorCode.getCode(), message);
    }

    public int getCode() {
        return code;
    }
}
