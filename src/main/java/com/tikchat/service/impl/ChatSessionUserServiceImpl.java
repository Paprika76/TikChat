package com.tikchat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tikchat.entity.ChatSessionUser;
import com.tikchat.entity.UserContact;
import com.tikchat.entity.dto.MessageSendDto;
import com.tikchat.entity.enums.MessageTypeEnum;
import com.tikchat.entity.enums.UserContactStatusEnum;
import com.tikchat.entity.enums.UserContactTypeEnum;
import com.tikchat.mapper.ChatSessionUserMapper;
import com.tikchat.mapper.UserContactMapper;
import com.tikchat.service.ChatSessionUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tikchat.websocket.MessageHandler;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 会话用户 服务实现类
 * </p>
 *
 * @author Paprika
 * @since 2024-07-16
 */
@Service
public class ChatSessionUserServiceImpl extends ServiceImpl<ChatSessionUserMapper, ChatSessionUser> implements ChatSessionUserService {

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private UserContactMapper userContactMapper;

    @Override
    public void updateRedundantInfo(String contactName, String contactId) {
        ChatSessionUser chatSessionUser = new ChatSessionUser();
        chatSessionUser.setContactName(contactName);
        QueryWrapper<ChatSessionUser> chatSessionUserQueryWrapper = new QueryWrapper<>();
        chatSessionUserQueryWrapper.eq("contact_id", contactId);
        chatSessionUserMapper.update(chatSessionUser,chatSessionUserQueryWrapper);
        //TODO 已完成 2.修改群信息发送一条ws消息 通知所有人说修改了  一般只要修改群昵称才需要这样做
        // 其实就只有一种情况: 修改群名才要提醒大家(解散等等也要 但那是其他的操作了)
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (contactTypeEnum==null){
            return ;
        }
        if(contactTypeEnum == UserContactTypeEnum.GROUP){
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());

            messageSendDto.setContactType(contactTypeEnum.getType());
//            messageSendDto.setMessageContent(MessageTypeEnum.CONTACT_NAME_UPDATE.getInitMessage());
            messageSendDto.setContactId(contactId);
//            messageSendDto.setContactName(groupName);//这个和后面一个写哪个都行
            messageSendDto.setExtendData(contactName);
            messageHandler.sendMessage(messageSendDto);
        }else{
            //说明是 人修改了昵称   所以要给所有好友都发消息说我更新了昵称
            QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
            userContactQueryWrapper.eq("contact_type", UserContactTypeEnum.USER);
            userContactQueryWrapper.eq("contact_id", contactId);
            userContactQueryWrapper.eq("status", UserContactStatusEnum.FRIEND);
            List<UserContact> userContactList = userContactMapper.selectList(userContactQueryWrapper);
            for (UserContact userContact:userContactList){
                MessageSendDto messageSendDto = new MessageSendDto();
                messageSendDto.setMessageType(MessageTypeEnum.CONTACT_NAME_UPDATE.getType());
                messageSendDto.setContactType(contactTypeEnum.getType());
//                messageSendDto.setMessageContent(MessageTypeEnum.CONTACT_NAME_UPDATE.getInitMessage());
                messageSendDto.setContactId(userContact.getUserId());
                messageSendDto.setSendUserId(contactId);
//            messageSendDto.setContactName(groupName);//这个和后面一个写哪个都行
                messageSendDto.setExtendData(contactName);
                messageSendDto.setSendUserNickName(contactName);

                messageHandler.sendMessage(messageSendDto);
            }
        }

    }
}
