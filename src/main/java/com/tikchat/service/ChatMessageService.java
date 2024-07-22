package com.tikchat.service;

import com.tikchat.entity.ChatMessage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tikchat.entity.dto.MessageSendDto;
import com.tikchat.entity.dto.TokenUserInfoDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * <p>
 * 聊天消息表 服务类
 * </p>
 *
 * @author Paprika
 * @since 2024-07-16
 */
public interface ChatMessageService extends IService<ChatMessage> {

    MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto);
    void saveMessageFile(String userId, String  messageId, MultipartFile file,MultipartFile cover);
    File downloadFile(String userId, Long fileId, Boolean showCover);

}
