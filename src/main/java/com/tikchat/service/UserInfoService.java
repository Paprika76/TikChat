package com.tikchat.service;

import com.tikchat.entity.UserInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tikchat.entity.vo.UserInfoVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 * 用户信息表 服务类
 * </p>
 *
 * @author Paprika
 * @since 2024-06-22
 */
public interface UserInfoService extends IService<UserInfo> {

    void register(String email, String nickName, String password);
    UserInfoVO login(String email, String password);
    UserInfo selectUserInfoById(String id);
    void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException;
    void updateUserStatus(Integer status, String userId);
    void forceOffLine(String userId);
}
