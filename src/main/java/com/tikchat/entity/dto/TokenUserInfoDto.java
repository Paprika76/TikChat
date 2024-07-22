package com.tikchat.entity.dto;

import java.io.Serializable;

public class TokenUserInfoDto implements Serializable {
    //dto是data transfer object  而vo是指value object
    //两者都是用来传code success data对象的 但是dto还有转换的逻辑实现
    private static final long serialVersionUID = -2136567784356732L;
    private String token;
    private String userId;
    private String nickName;
    private Boolean admin=false;

    public TokenUserInfoDto() {
    }

    public TokenUserInfoDto(String userId, String nickName, Boolean admin) {
        this.userId = userId;
        this.nickName = nickName;
        this.admin = admin;
    }

    public TokenUserInfoDto(String token, String userId, String nickName, Boolean admin) {
        this.token = token;
        this.userId = userId;
        this.nickName = nickName;
        this.admin = admin;
    }




    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

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

    public Boolean getAdmin() {
        return admin;
    }

    public void setAdmin(Boolean admin) {
        this.admin = admin;
    }
}
