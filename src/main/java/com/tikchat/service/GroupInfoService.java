package com.tikchat.service;

import com.tikchat.entity.GroupInfo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.entity.enums.MessageTypeEnum;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
public interface GroupInfoService extends IService<GroupInfo> {

    void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile,MultipartFile avatarCover) throws IOException;
    void dissoluteGroup(String groupOwnerId,String groupId);
    void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String selectContact, Integer opType);
    void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum);
}
