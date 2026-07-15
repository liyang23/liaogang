package com.liaogang.famou.km.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应包装类（v0.32 PRD §5.1 通用响应）。
 *
 * <p>code: 0 表示成功；其他表示失败
 * <p>msg: 提示信息
 * <p>data: 业务数据
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 0 = 成功 */
    public static final int CODE_SUCCESS = 0;

    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(CODE_SUCCESS);
        r.setMsg("ok");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }

    public static <T> Result<T> fail(String msg) {
        return fail(500, msg);
    }
}
