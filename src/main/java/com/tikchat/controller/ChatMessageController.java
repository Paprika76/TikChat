
package com.tikchat.controller;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;


import com.tikchat.service.ChatMessageService;


import org.springframework.stereotype.Controller;

/**
 * <p>
 * 聊天消息表 前端控制器
 * </p>
 *
 * @author Paprika
 * @since 2024-07-16
 */
@Controller
@RequestMapping("")
public class ChatMessageController {
    @Resource
    private ChatMessageService chatMessageService;


}

