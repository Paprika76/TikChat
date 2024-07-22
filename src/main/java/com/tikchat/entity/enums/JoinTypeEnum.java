package com.tikchat.entity.enums;

public enum JoinTypeEnum {
    JOIN(1,"直接加入"),APPLY(0,"需要审核");
    private Integer type;
    private String desc;

    JoinTypeEnum(Integer type, String desc) {
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
