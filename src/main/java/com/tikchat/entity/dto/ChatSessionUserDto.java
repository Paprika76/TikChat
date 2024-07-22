package com.tikchat.entity.dto;

import com.tikchat.entity.enums.UserContactTypeEnum;
import com.tikchat.utils.StringTools;

/**
 * <p>
 * 会话用户
 * </p>
 *
 * @author Paprika
 * @since 2024-07-16
 */
public class ChatSessionUserDto {
    private String userId;

    private String contactId;

    private String sessionId;

    private String contactName;

    private String lastMessage;

    private String lastReceiveTime;

    private Integer memberCount;

    private Integer contactType;


    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastReceiveTime() {
        return lastReceiveTime;
    }

    public void setLastReceiveTime(String lastReceiveTime) {
        this.lastReceiveTime = lastReceiveTime;
    }

    public Integer getContactType() {
        if (StringTools.isEmpty(contactId)){
            return null;
        }
        return UserContactTypeEnum.getByPrefix(contactId).getType();
    }

    public void setContactType(Integer contactType) {
        this.contactType = contactType;
    }
}
