package com.tikchat.entity.enums;

import com.tikchat.utils.StringTools;

public enum UserContactApplyStatusEnum {
    INIT(0,"待处理"),
    PASS(1,"已同意"),
    REJECT(2,"已拒绝"),
    BLACK(3,"已拉黑");


    private Integer status;
    private String desc;

    UserContactApplyStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    //即以下两个函数是一个根据enumName 一个是根据status来的
    public static UserContactApplyStatusEnum getByStatus(String enumName){
        try {
            if(StringTools.isEmpty(enumName)){
                return null;
            }
            return UserContactApplyStatusEnum.valueOf(enumName.toUpperCase());
        }catch (IllegalArgumentException e){
            return null;
        }
    }

    public static UserContactApplyStatusEnum getByStatus(Integer status){
        for (UserContactApplyStatusEnum item: UserContactApplyStatusEnum.values()){
            if(item.getStatus().equals(status)){
                return item;
            }
        }
        return null;
    }




    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
