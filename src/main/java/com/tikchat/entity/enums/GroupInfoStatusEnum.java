package com.tikchat.entity.enums;

public enum GroupInfoStatusEnum {
    //五种！
    NORMAL(1,"群聊正常"),
    DISMISSED(0,"群聊解散"),
    ;
    private Integer status;
    private String desc;

    GroupInfoStatusEnum(Integer status, String desc) {
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
