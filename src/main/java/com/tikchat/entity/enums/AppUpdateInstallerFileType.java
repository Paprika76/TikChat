package com.tikchat.entity.enums;

public enum AppUpdateInstallerFileType {
    LOCAL(0,"本地下载安装包"),
    OUTER(1,"外链下载安装包"),
    ;

    private Integer type;
    private String desc;

    AppUpdateInstallerFileType(Integer type, String desc) {
        this.type = type;
        this.desc = desc;
    }



    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
