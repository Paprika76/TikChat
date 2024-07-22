package com.tikchat.entity.enums;

public enum BeautyAccountStatusEnum {
    NO_USE(0),USED(1);
    private Integer status;

    BeautyAccountStatusEnum(Integer status) {
        this.status = status;
    }

    public Integer getStatus() {
        return status;
    }
}
