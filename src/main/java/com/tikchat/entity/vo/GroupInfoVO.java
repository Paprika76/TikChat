package com.tikchat.entity.vo;

import com.tikchat.entity.UserContact;
import com.tikchat.entity.UserInfo;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */

public class GroupInfoVO implements Serializable {

    private static final long serialVersionUID = -32645671232543L;

//    private GroupInfo groupInfo;

    private UserContact UserContact;

    private List<UserContact> userContactList;

    private String groupId;

    private String groupName;

    private String groupOwnerId;

    private String groupNotice;

    private Integer joinType;

    private Integer status;

//    private Date createTime;
//
//    private Date updateTime;
//
//    private Integer deleted;
//
//    private Integer version;


    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupOwnerId() {
        return groupOwnerId;
    }

    public void setGroupOwnerId(String groupOwnerId) {
        this.groupOwnerId = groupOwnerId;
    }

    public String getGroupNotice() {
        return groupNotice;
    }

    public void setGroupNotice(String groupNotice) {
        this.groupNotice = groupNotice;
    }

    public Integer getJoinType() {
        return joinType;
    }

    public void setJoinType(Integer joinType) {
        this.joinType = joinType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

//    public Date getCreateTime() {
//        return createTime;
//    }
//
//    public void setCreateTime(Date createTime) {
//        this.createTime = createTime;
//    }
//
//    public Date getUpdateTime() {
//        return updateTime;
//    }
//
//    public void setUpdateTime(Date updateTime) {
//        this.updateTime = updateTime;
//    }
//
//    public Integer getDeleted() {
//        return deleted;
//    }
//
//    public void setDeleted(Integer deleted) {
//        this.deleted = deleted;
//    }
//
//    public Integer getVersion() {
//        return version;
//    }
//
//    public void setVersion(Integer version) {
//        this.version = version;
//    }




    private Integer memberCount;


    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public List<UserContact> getUserContactList() {
        return userContactList;
    }

    public void setUserContactList(List<UserContact> userContactList) {
        this.userContactList = userContactList;
    }

//    public GroupInfo getGroupInfo() {
//        return groupInfo;
//    }
//
//    public void setGroupInfo(GroupInfo groupInfo) {
//        this.groupInfo = groupInfo;
//    }
    private List<UserInfo> userInfoList;

    public List<UserInfo> getUserInfoList() {
        return userInfoList;
    }

    public void setUserInfoList(List<UserInfo> userInfoList) {
        this.userInfoList = userInfoList;
    }

    public com.tikchat.entity.UserContact getUserContact() {
        return UserContact;
    }

    public void setUserContact(com.tikchat.entity.UserContact userContact) {
        UserContact = userContact;
    }
}
