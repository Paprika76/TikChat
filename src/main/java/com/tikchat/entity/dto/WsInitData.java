package com.tikchat.entity.dto;

import com.tikchat.entity.ChatMessage;
import com.tikchat.entity.ChatSessionUser;

import java.util.List;

/**
 * @FileName WsInitData
 * @Description ws初始化信息
 * @Author Paprika
 * @date 编写时间 2024-07-16 23:11
 **/

public class WsInitData {
    private List<ChatSessionUser> chatSessionList;

    private List<ChatMessage> chatMessageList;

    private Integer applyCount; //这个是获取申请的条数 每次显示在新朋友那里 每次wsInit都会

    public List<ChatSessionUser> getchatSessionList() {
        return chatSessionList;
    }

    public void setchatSessionList(List<ChatSessionUser> chatSessionList) {
        this.chatSessionList = chatSessionList;
    }

    public List<ChatMessage> getChatMessageList() {
        return chatMessageList;
    }

    public void setChatMessageList(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    public Integer getApplyCount() {
        return applyCount;
    }

    public void setApplyCount(Integer applyCount) {
        this.applyCount = applyCount;
    }
}
