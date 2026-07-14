package com.liaogang.famou.km.common;

import lombok.Getter;

/**
 * 业务异常（v0.32 PRD §5.1）。
 *
 * <p>通过 GlobalExceptionHandler 统一返回 Result.fail(code, msg)
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 业务错误码（与前端约定） */
    private final int code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
