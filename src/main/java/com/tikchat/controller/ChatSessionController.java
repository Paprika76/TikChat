
package com.tikchat.controller;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;


import com.tikchat.service.ChatSessionService;


import org.springframework.stereotype.Controller;

/**
 * <p>
 * 会话信息 前端控制器
 * </p>
 *
 * @author Paprika
 * @since 2024-07-16
 */
@Controller
@RequestMapping("")
public class ChatSessionController {
    @Resource
    private ChatSessionService chatSessionService;


}

