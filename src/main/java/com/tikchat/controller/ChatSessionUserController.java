
package com.tikchat.controller;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;


import com.tikchat.service.ChatSessionUserService;


import org.springframework.stereotype.Controller;

/**
 * <p>
 * 会话用户 前端控制器
 * </p>
 *
 * @author Paprika
 * @since 2024-07-16
 */
@Controller
@RequestMapping("/tikchat/chat-session-user")
public class ChatSessionUserController {
    @Resource
    private ChatSessionUserService chatSessionUserService;


}

