
package com.tikchat.controller;

import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;


import com.tikchat.service.UserContactApplyService;

/**
 * <p>
 * 联系人申请 前端控制器
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
@RestController("userContactApplyController")
@RequestMapping("/tikchat/user-contact-apply")
public class UserContactApplyController extends ABaseControllertest{
    @Resource
    private UserContactApplyService userContactApplyService;


}

