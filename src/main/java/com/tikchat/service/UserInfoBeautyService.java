package com.tikchat.service;

import com.tikchat.entity.UserInfoBeauty;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 靓号表
 服务类
 * </p>
 *
 * @author Paprika
 * @since 2024-06-22
 */
public interface UserInfoBeautyService extends IService<UserInfoBeauty> {
    void saveBeautyAccount(UserInfoBeauty userInfoBeauty);
    void saveBeautyAccount2(Integer id,String userId,String email);
}
