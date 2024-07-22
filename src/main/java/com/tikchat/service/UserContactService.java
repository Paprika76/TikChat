package com.tikchat.service;

import com.tikchat.entity.UserContact;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tikchat.entity.UserInfo;
import com.tikchat.entity.dto.UserContactSearchResultDto;
import com.tikchat.entity.enums.UserContactStatusEnum;

/**
 * <p>
 * 联系人 服务类
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
public interface UserContactService extends IService<UserContact> {
    UserContactSearchResultDto searchContact(String userId, String contactId);

//    Integer loadApply(TokenUserInfoDto tokenUserInfoDto,String contactId,String applyInfo);

    void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum);

    //这个是真的添加联系人:
    /*a添加b  b添加a!!!!!!!!!!!!!!!!!!!*/
    void addContact(String applyUserId, String receiveUserId, String contactId,Integer contactType,String applyInfo);

    void addContact4Robot(String applyUserId);
    UserInfo addChatSessionUserAndGetRearUserInfo(String sessionId, String applyUserId, String contactId);


}
