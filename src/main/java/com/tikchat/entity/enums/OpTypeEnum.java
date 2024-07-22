package com.tikchat.entity.enums;

public enum OpTypeEnum {
    ADD(0,"新增"),
    Remove(1,"移除"),
    ;
    private Integer type;

    private String desc;

    OpTypeEnum(Integer type, String desc) {
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
