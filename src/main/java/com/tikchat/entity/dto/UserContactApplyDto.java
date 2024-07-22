package com.tikchat.entity.dto;

import com.tikchat.entity.enums.UserContactApplyStatusEnum;

import java.util.Date;

public class UserContactApplyDto {

    private static final long serialVersionUID = 2345634786523L;

    private Integer applyId;

    private String applyUserId;

    private String receiveUserId;

    private Integer contactType;

    private String contactId;

    private Long lastApplyTime;

    private Integer status;

    private String applyInfo;


    private Date createTime;

    private Date updateTime;

    private Integer deleted;

    private Integer version;


    //在原entity类的基础上的加的东西
    private String nickName;

    private String groupName;

    private String statusName;//本来返回去是null的  因为未初始化  但是我在getter里面写了是根据status来的！！！！！！



    public Integer getApplyId() {
        return applyId;
    }

    public void setApplyId(Integer applyId) {
        this.applyId = applyId;
    }

    public String getApplyUserId() {
        return applyUserId;
    }

    public void setApplyUserId(String applyUserId) {
        this.applyUserId = applyUserId;
    }

    public String getReceiveUserId() {
        return receiveUserId;
    }

    public void setReceiveUserId(String receiveUserId) {
        this.receiveUserId = receiveUserId;
    }

    public Integer getContactType() {
        return contactType;
    }

    public void setContactType(Integer contactType) {
        this.contactType = contactType;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public Long getLastApplyTime() {
        return lastApplyTime;
    }

    public void setLastApplyTime(Long lastApplyTime) {
        this.lastApplyTime = lastApplyTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getApplyInfo() {
        return applyInfo;
    }

    public void setApplyInfo(String applyInfo) {
        this.applyInfo = applyInfo;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getDeleted() {
        return deleted;
    }

    public void setDeleted(Integer deleted) {
        this.deleted = deleted;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getStatusName() {

        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);
        return statusEnum==null?null:statusEnum.getDesc();
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }
}
