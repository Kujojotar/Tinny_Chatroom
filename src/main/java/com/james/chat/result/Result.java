package com.james.chat.result;

public class Result<T> {

    private Integer code;
    private Boolean success;
    private String msg;
    private T obj;

    public Result(Integer code, Boolean success, String msg, T obj) {
        this.code = code;
        this.success = success;
        this.msg = msg;
        this.obj = obj;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getObj() {
        return obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }
}
