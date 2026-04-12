package com.jcen.unifit.exception;

import com.jcen.unifit.common.BaseResponse;
import com.jcen.unifit.common.ErrorCode;
import com.jcen.unifit.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<Void> businessExceptionHandler(BusinessException e) {
        log.warn("business exception, code={}, message={}", e.getCode(), e.getMessage());
        return new BaseResponse<>(e.getCode(), null, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<Void> runtimeExceptionHandler(Exception e) {
        log.error("system exception", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}
