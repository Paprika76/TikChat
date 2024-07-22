package com.tikchat.service;

import com.tikchat.entity.ChatSessionUser;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 会话用户 服务类
 * </p>
 *
 * @author Paprika
 * @since 2024-07-16
 */
public interface ChatSessionUserService extends IService<ChatSessionUser> {
    void updateRedundantInfo(String contactName, String contactId);
}
