package com.tikchat.entity.vo;

public class ResultMap<T> {
    private boolean success;
    private String msg;
    private T data;

//    private Integer code;
//    private String info;

    public ResultMap(boolean success, String msg, T data) {
        this.success = success;
        this.msg = msg;
        this.data = data;
    }

    // 构造函数2：只返回 success 和 msg，不包含 data
    public ResultMap(boolean success, String msg) {
        this.success = success;
        this.msg = msg;
        this.data = null; // 可以不用初始化，因为 data 不会使用
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }


}
