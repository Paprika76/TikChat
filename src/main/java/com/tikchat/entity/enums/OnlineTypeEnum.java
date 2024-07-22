package com.tikchat.entity.enums;

public enum OnlineTypeEnum {
    ONLINE(1,"在线"),
    OFFLINE(0,"在线"),
    ;
    private Integer type;
    private String desc;

    OnlineTypeEnum(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    public Integer getType() {
        return type;
    }


    public String getDesc() {
        return desc;
    }

}
