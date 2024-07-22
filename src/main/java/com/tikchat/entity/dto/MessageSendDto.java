package com.tikchat.entity.dto;

import com.tikchat.utils.StringTools;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessageSendDto<T> implements Serializable {
    private static final long serialVersionUID = -21335617675632L;
    //消息ID
    private Long messageId;
    //会话ID
    private String sessionId;
    //发送人
    private String sendUserId;//个人感觉这个可能是群Id  “群”发消息给用户时，那就是群是sendUserId了
    //发送人昵称
    private String sendUserNickName;
    //联系人ID
    private String contactId;
    //联系人名称
    private String contactName;

    //消息内容   tmd这到底是什么  和lastMessage到底什么区别!!!!!!!!
    private String messageContent;
    //最后的消息
    private String lastMessage;

    //消息类型
    private Integer messageType;
    //发送时间
    private Long sendTime;
    //联系人类型
    private Integer contactType;
    //扩展信息
    private T extendData;

    //消息状态 0:发送中  1:已发送 对于文件是异步上传用状态处理
    private Integer status;

    private Integer memberCount;

    public Long getMessageId() {
        return messageId;
    }

    public void setMessageId(Long messageId) {
        this.messageId = messageId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getSendUserId() {
        return sendUserId;
    }

    public void setSendUserId(String sendUserId) {
        this.sendUserId = sendUserId;
    }

    public String getSendUserNickName() {
        return sendUserNickName;
    }

    public void setSendUserNickName(String sendUserNickName) {
        this.sendUserNickName = sendUserNickName;
    }

    public String getContactId() {
        return contactId;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public void setMessageContent(String messageContent) {
        this.messageContent = messageContent;
    }

    //TODO 这个看不懂
    /*这个看不懂!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!*/
    public String getLastMessage() {
        if(StringTools.isEmpty(lastMessage)){
            return messageContent;
        }
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }




    public Integer getMessageType() {
        return messageType;
    }

    public void setMessageType(Integer messageType) {
        this.messageType = messageType;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public Integer getContactType() {
        return contactType;
    }

    public void setContactType(Integer contactType) {
        this.contactType = contactType;
    }

    public T getExtendData() {
        return extendData;
    }

    public void setExtendData(T extendData) {
        this.extendData = extendData;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(Integer memberCount) {
        this.memberCount = memberCount;
    }
}
