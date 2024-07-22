package com.tikchat.entity.enums;

public enum AppUpdateStatusEnum {
    INIT(0,"未发布 或 取消发布"),
    GRAYSCALE(1,"灰度发布"),
    ALL(2,"全网发布"),
    ;
    private Integer status;
    private String desc;

    AppUpdateStatusEnum(Integer status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public static AppUpdateStatusEnum getByStatus(Integer status){
        for (AppUpdateStatusEnum appUpdateStatusEnum: AppUpdateStatusEnum.values()){
            if(appUpdateStatusEnum.status.equals(status)){
                return appUpdateStatusEnum;
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
