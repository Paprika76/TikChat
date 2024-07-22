package com.tikchat.service.impl;

import com.tikchat.entity.ChatSession;
import com.tikchat.mapper.ChatSessionMapper;
import com.tikchat.service.ChatSessionService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 会话信息 服务实现类
 * </p>
 *
 * @author Paprika
 * @since 2024-07-16
 */
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession> implements ChatSessionService {

}
