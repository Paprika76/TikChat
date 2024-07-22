package com.tikchat.entity.dto;

public class UserContactSearchResultDto {
    //是两种contact的结合体的形式

    private String contactId;
    private String contactType;//好友还是群
    private String nickNameOrGroupName;//群名或者是好友名
//    private Long avatarLastUpdate;
    private Integer status;
    private String statusName;
    private Integer sex;
    private String areaName;

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getnickNameOrGroupName() {
        return nickNameOrGroupName;
    }

    public void setnickNameOrGroupName(String nickNameOrGroupName) {
        this.nickNameOrGroupName = nickNameOrGroupName;
    }

//    public Long getAvatarLastUpdate() {
//        return avatarLastUpdate;
//    }
//
//    public void setAvatarLastUpdate(Long avatarLastUpdate) {
//        this.avatarLastUpdate = avatarLastUpdate;
//    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }
}
