package com.tikchat.service;

import com.tikchat.entity.UserContactApply;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tikchat.entity.dto.TokenUserInfoDto;

/**
 * <p>
 * 联系人申请 服务类
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
public interface UserContactApplyService extends IService<UserContactApply> {


    Integer applyAdd(TokenUserInfoDto tokenUserInfoDto,String contactId,String applyInfo);

    void dealWithApply(TokenUserInfoDto tokenUserInfoDto,Integer applyId,Integer status);


}
