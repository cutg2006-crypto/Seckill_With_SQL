package com.hzr.seckillwithsql.exception;

import com.hzr.seckillwithsql.common.BaseResponse;
import com.hzr.seckillwithsql.common.ErrorCode;
import com.hzr.seckillwithsql.common.ResultUtils;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e){
        log.error(e.getMessage());
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> handleRuntimeException(RuntimeException e){
        log.error(e.getMessage());
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR, "系统错误");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public BaseResponse<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public BaseResponse<?> handleConstraintViolation(ConstraintViolationException e) {
        String message = e.getConstraintViolations().stream().map(ConstraintViolation::getMessage).findFirst().orElse("请求参数校验失败");
        return ResultUtils.error(ErrorCode.PARAMS_ERROR, message);
    }
}
