package com.tikchat.entity.enums;

public enum MessageStatusEnum {
    SENDING(0,"发送中"),
    FINISHED(1,"已发送"),
    ;
    private Integer status;
    private String desc;

    MessageStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public Integer getStatus() {
        return status;
    }

    public String getDesc() {
        return desc;
    }
}
