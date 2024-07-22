package com.tikchat.entity.vo;

import java.io.Serializable;

public class UserInfoVO implements Serializable {
    //存放登陆后的所有用户相关信息  这个VO专门给accountController的login的返回值的data使用




    private static final long serialVersionUID = -2365468765343L;
    private String userId;
    private String nickName;

    /**
     * 0:女 1:男
     */
    private Integer sex;
//    private Integer joinType;
    private String personalSignature;
    private String areaCode;
    private String areaName;
    private String token;
    private Boolean admin;

    private Integer contactStatus;



    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

//    public Integer getJoinType() {
//        return joinType;
//    }
//
//    public void setJoinType(Integer joinType) {
//        this.joinType = joinType;
//    }

    public String getPersonalSignature() {
        return personalSignature;
    }

    public void setPersonalSignature(String personalSignature) {
        this.personalSignature = personalSignature;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(String areaCode) {
        this.areaCode = areaCode;
    }

    public String getAreaName() {
        return areaName;
    }

    public void setAreaName(String areaName) {
        this.areaName = areaName;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }

    public Integer getContactStatus() {
        return contactStatus;
    }

    public void setContactStatus(Integer contactStatus) {
        this.contactStatus = contactStatus;
    }
}

