package com.swxt.manager.config;


public class BusinessException extends RuntimeException {

    
    private final Core.ResultCode resultCode;

    public BusinessException(Core.ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public BusinessException(Core.ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.resultCode = resultCode;
    }

    public Core.ResultCode getResultCode() {
        return resultCode;
    }
}
