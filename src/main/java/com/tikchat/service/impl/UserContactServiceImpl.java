package com.tikchat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tikchat.entity.*;
import com.tikchat.entity.dto.*;
import com.tikchat.entity.enums.*;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.*;
import com.tikchat.redis.RedisComponent;
import com.tikchat.service.UserContactApplyService;
import com.tikchat.service.UserContactService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tikchat.service.component.ServiceComponent;
import com.tikchat.utils.CopyTools;
import com.tikchat.utils.StringTools;
import com.tikchat.websocket.ChannelContextUtils;
import com.tikchat.websocket.MessageHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 联系人 服务实现类
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
@Service
public class UserContactServiceImpl extends ServiceImpl<UserContactMapper, UserContact> implements UserContactService {

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private ServiceComponent serviceComponent;

    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    @Resource
    private UserContactApplyService userContactApplyService;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private MessageHandler messageHandler;


    @Resource
    private ChannelContextUtils channelContextUtils;



    @Override
    public UserContactSearchResultDto searchContact(String userId, String contactId) {
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (typeEnum==null){
            return null;
        }
        UserContactSearchResultDto resultDto = new UserContactSearchResultDto();
        switch (typeEnum){
            case USER:
                //通过userId查
                UserInfo userInfo = userInfoMapper.selectById(contactId);
                if (userInfo==null){
                    return null;
                }
                resultDto = CopyTools.copy(userInfo, UserContactSearchResultDto.class);
                break;
            case GROUP:
                //通过groupId查
                GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
                if (groupInfo==null){
                    return null;
                }
//            resultDto = CopyTools.copy(groupInfo, UserContactSearchResultDto.class);
                resultDto.setnickNameOrGroupName(groupInfo.getGroupName());
                break;
        }
        resultDto.setContactType(typeEnum.toString());
        resultDto.setContactId(contactId);
        //下面的其实可以注释掉因为我们有时候想要和自己聊天  下面的代码是为了阻止和自己聊天
//        //如果是自己的话那就不显示其他的比如和联系人聊天什么的
//        if(userId.equals(contactId)){
//            resultDto.setStatus(UserContactStatusEnum.FRIEND.getStatus());
//            return resultDto;
//        }
        //查询的联系人是否是好友关系
        UserContact userContact = serviceComponent.selectByUserIdAndContactId(userId,contactId);
        resultDto.setStatus(userContact==null?null:userContact.getStatus());
        System.out.println(resultDto);
        return resultDto;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeUserContact(String userId, String contactId, UserContactStatusEnum statusEnum) {
        //TODO 这里还有一个很大的问题 contactId分为人和群 肯定是要区分的  推出群聊和群聊踢出你去！！！！


        //两边都要删除或拉黑
        //1.我删他 只需要改状态就行
        /*到底应该用select的基础上再set一个属性在update还是new一个？？？？？？？？？？
        * 实测两个都行  说明确实只要new一个就行  不必先搜一次了 浪费时间*/
        /*1.*/
//        UserContact userContact = serviceComponent.selectByUserIdAndContactId(userId, contactId);
        /*2.*/
        UserContact userContact = new UserContact();
        userContact.setStatus(statusEnum.getStatus());//删除状态！！！！
        this.updateByUserIdAndContactId(userContact,userId,contactId);

        //他删我
//        UserContact friendUserContact = serviceComponent.selectByUserIdAndContactId(contactId, userId);
        UserContact friendUserContact = new UserContact();
        if (statusEnum.equals(UserContactStatusEnum.BLACK_FRIEND)){
            friendUserContact.setStatus(UserContactStatusEnum.FRIEND_BLACK_ME.getStatus());
        }else if (statusEnum.equals(UserContactStatusEnum.DELETED_FRIEND)){
            friendUserContact.setStatus(UserContactStatusEnum.FRIEND_DELETED_ME.getStatus());
        }
        this.updateByUserIdAndContactId(friendUserContact,contactId,userId);

        //TODO 已完成 从我的好友列表缓存中删除好友 真的删除  还有从好友列表中删除我
        // 但是在数据库中不真的删除(只是设一下删除好友状态 在前面几行代码已实现) 在缓存中是真的删除
        redisComponent.removeContactIdFromList(userId,contactId);
        redisComponent.removeContactIdFromList(contactId,userId);

    }



    public void updateByUserIdAndContactId(UserContact userContact,String userId, String contactId){
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.eq("user_id", userId);
        userContactQueryWrapper.eq("contact_id", contactId);
        userContactMapper.update(userContact,userContactQueryWrapper);
    }

    @Override
    public void addContact(String applyUserId, String receiveUserId, String contactId, Integer contactType, String applyInfo) {
        //添加联系人的实现！！！！！！！！！！！！！
        //每次都是先判断各个参数对不对  但是这是添加联系人  所以更松了  在添加联系人之前一般都已经校验过了
        //如果是人的话那两边都要互相加对方为好友
        //如果是群的话  只要人加群就行
        if (contactType.equals(UserContactTypeEnum.GROUP.getType())) {
            //判断群有没有满了
            QueryWrapper<UserContact> groupInfoQueryWrapper = new QueryWrapper<>();
            groupInfoQueryWrapper.eq("contact_id", contactId);
            groupInfoQueryWrapper.eq("status", UserContactStatusEnum.FRIEND.getStatus());
            Integer count = userContactMapper.selectCount(groupInfoQueryWrapper);
            Integer maxGroupCount = redisComponent.getSysSetting().getMaxGroupCount();
            if (count >= maxGroupCount) {
                throw new BusinessException("成员已满，无法加入");
            }
        }
        //基本排漏洞完毕 接下来就真的双方添加好友了
//        Date curDate = new Date();
        //同意，双方添加好友
//        List<UserContact> userContacts = new ArrayList<>();
        //申请人添加对方
        UserContact userContact = new UserContact();
        userContact.setUserId(applyUserId);
        userContact.setContactId(contactId);
        userContact.setContactType(contactType);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());

        this.insertOrUpdateUserContact(userContact,applyUserId,contactId);
//        userContacts.add(userContact);
//            userContactService.saveOrUpdate(userContact);
        //如果不是添加群的话 那么接收人添加申请人为好友
        if (contactType.equals(UserContactTypeEnum.USER.getType())){
            UserContact userContact2 = new UserContact();
            userContact2.setUserId(contactId);
            userContact2.setContactId(applyUserId);
            userContact2.setContactType(contactType);
            userContact2.setStatus(UserContactStatusEnum.FRIEND.getStatus());
//            userContacts.add(userContact2);
            this.insertOrUpdateUserContact(userContact2,contactId,applyUserId);

//            userContactMapper.insert(userContact2);
//                userContactService.saveOrUpdate(userContact2);
        }
        //redis中也要添加好友：以下操作
        //操作完后 TODO 已完成 如果是好友申请的话 双方也在redis缓存里面添加好友到好友列表 添加缓存
        if(UserContactTypeEnum.USER.getType().equals(contactType)){// 是好友的话那就要加两遍  a加b b加a
            redisComponent.addContactId(receiveUserId,applyUserId);
        }
        redisComponent.addContactId(applyUserId,contactId);// contactId可能是群主也可能是收到好友申请的人
        //TODO 已完成 创建会话  ws发送消息 添加好友的信息
        String sessionId = null;
        if(UserContactTypeEnum.USER.getType().equals(contactType)){// 是好友的话那就要加两遍  a加b b加a
            //加好友通过的话 那就是 a->b b->a："我们已经是好友啦，一起来聊天吧！"
            sessionId = StringTools.getChatSessionId4User(new String[]{applyUserId,contactId});
            //1.创建会话 增加会话信息 ChatSession表
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(sessionId);
            chatSession.setLastMessage(applyInfo);
            chatSession.setLastReceiveTime(System.currentTimeMillis());
            ChatSession dbChatMessage = chatSessionMapper.selectById(sessionId);
            if(dbChatMessage==null){
                chatSessionMapper.insert(chatSession);
            }else{
                chatSessionMapper.updateById(chatSession);
            }
            //2.增加会话人信息 ChatSessionUser表  a增加 b也增加
            addChatSessionUserAndGetRearUserInfo(sessionId,applyUserId,contactId);
            UserInfo applyUserInfo = addChatSessionUserAndGetRearUserInfo(sessionId, contactId, applyUserId);

            //3.增加聊天消息信息 ChatMessage表
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(sessionId);
            chatMessage.setMessageContent(applyInfo);
            chatMessage.setMessageType(MessageTypeEnum.ADD_FRIEND.getType());
            chatMessage.setSendUserId(applyUserId);
            chatMessage.setContactId(contactId);
            chatMessage.setStatus(MessageStatusEnum.FINISHED.getStatus());
            chatMessage.setSendTime(System.currentTimeMillis());
            chatMessage.setSendUserNickName(applyUserInfo.getNickName());
            chatMessageMapper.insert(chatMessage);


            //4.最后一步.发消息  apply->receive
            // apply直接发送applyInfo的消息给receive(和微信一模一样)
            // apply还要给自己发消息  因为是确定的一个sessionId内部  所以是只在这个session里面看得到是自己发消息给自己的!!!!!!!
            //b(receive)中是这样显示的:| a  | (a发给b的消息)     a(apply)中是这样显示的:|    |  a给自己发消息就是显示在右边的!!!!!!!
            //                       |    |                                       |   a|
            MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
            messageHandler.sendMessage(messageSendDto);//这里的MessageType就是ADD_FRIEND的

            //给自己发消息 messageSendDto要改成自己
            messageSendDto.setMessageType(MessageTypeEnum.ADD_FRIEND_SELF.getType());//前端就是根据这个来判断是显示在左边还是右边
            messageSendDto.setContactId(applyUserId);
//            messageSendDto.setExtendData(userContact);//TODO 这里没搞清楚!!!!!!!!!!!!!!!!!!!!!!!!!!! 为什么还要ExtendData  先不用管 我感觉老罗写的有问题
            messageHandler.sendMessage(messageSendDto);



        }else{//同意别人加入本群  把人加进群来
            //1.chatSession表
            String groupSessionId = StringTools.getChatSessionId4Group(contactId);
//            String groupSessionId = StringTools.getChatSessionId4Group(groupInfo.getGroupId());
            UserInfo userInfo = userInfoMapper.selectById(applyUserId);
            String sendMessage = String.format(MessageTypeEnum.ADD_GROUP.getInitMessage(), userInfo.getNickName());
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(groupSessionId);
            chatSession.setLastMessage(sendMessage);
            chatSession.setLastReceiveTime(System.currentTimeMillis());
            ChatSession dbChatMessage = chatSessionMapper.selectById(groupSessionId);
            if(dbChatMessage==null){
                chatSessionMapper.insert(chatSession);
            }else{
                chatSessionMapper.updateById(chatSession);
            }


            //ChatSessionUser表
            ChatSessionUser chatSessionUser = new ChatSessionUser();
            chatSessionUser.setUserId(applyUserId);
            chatSessionUser.setSessionId(groupSessionId);
            chatSessionUser.setContactId(contactId);
            chatSessionUser.setCreateTime(new Date());
            GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
            chatSessionUser.setContactName(groupInfo.getGroupName());
            QueryWrapper<ChatSessionUser> chatSessionUserQueryWrapper = new QueryWrapper<>();
            Integer count = chatSessionUserMapper.selectCount(chatSessionUserQueryWrapper);
            if(count==0){
                chatSessionUserMapper.insert(chatSessionUser);
            }else{
                chatSessionUserMapper.update(chatSessionUser,chatSessionUserQueryWrapper);
                chatSessionUserMapper.updateById(chatSessionUser);
            }


            // 3.增加聊天消息信息 ChatMessage表
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(groupSessionId);
            chatMessage.setMessageContent(sendMessage);
            chatMessage.setMessageType(MessageTypeEnum.ADD_GROUP.getType());
            //这两个不用
//            chatMessage.setSendUserId(groupInfo.getGroupOwnerId());
//            chatMessage.setSendUserNickName(applyUserInfo.getNickName());
            chatMessage.setContactId(contactId);
            chatMessage.setStatus(MessageStatusEnum.FINISHED.getStatus());
            chatMessage.setSendTime(System.currentTimeMillis());
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessageMapper.insert(chatMessage);

            redisComponent.addContactId(applyUserId,contactId);// contactId可能是群主也可能是收到好友申请的人
            //将联系人添加到群组通道channel中接收消息
            // TODO 注意: 这个容易漏掉!!!!!!!   因为这个是为了要把人添加进群里才能聊天!!!  还要时刻注意参数的前后顺序
            channelContextUtils.add2Group(contactId,applyUserId);



            //“群”发一条消息给他
            MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
//            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setSendUserId(contactId);//群Id
            messageSendDto.setContactType(UserContactTypeEnum.GROUP.getType());//这样才会群发
            messageSendDto.setMessageContent(MessageTypeEnum.ADD_GROUP.getInitMessage());//不能硬编码
            messageSendDto.setContactName(groupInfo.getGroupName());

            //获取群人数
            QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
            userContactQueryWrapper.eq("contact_id", contactId);
            userContactQueryWrapper.eq("status", UserContactStatusEnum.FRIEND.getStatus());
            Integer memberCount = userContactMapper.selectCount(userContactQueryWrapper);
            messageSendDto.setMemberCount(memberCount);
            messageHandler.sendMessage(messageSendDto);
        }


    }
    //AndGetRearUserInfo的意思是会返回第三个参数contactId的UserInfo!!!!!!!!!!!!!!!!!!!!!!!
    public UserInfo addChatSessionUserAndGetRearUserInfo(String sessionId,String applyUserId,String contactId){
        UserInfo userInfo = userInfoMapper.selectById(contactId);
        String contactNickName = userInfo.getNickName();
//        ChatSessionUserDto chatSessionUserDto = new ChatSessionUserDto();
        ChatSessionUser chatSessionUser = new ChatSessionUser();
        chatSessionUser.setUserId(applyUserId);
        chatSessionUser.setSessionId(sessionId);
        chatSessionUser.setContactId(contactId);
        chatSessionUser.setContactName(contactNickName);

        QueryWrapper<ChatSessionUser> chatSessionUserQueryWrapper = new QueryWrapper<>();
        chatSessionUserQueryWrapper.eq("user_id", applyUserId);
        chatSessionUserQueryWrapper.eq("contact_id", contactId);
        chatSessionUserQueryWrapper.eq("session_id", sessionId);
        List<ChatSessionUser> chatSessionUsers = chatSessionUserMapper.selectList(chatSessionUserQueryWrapper);
        if(chatSessionUsers.isEmpty()){
            chatSessionUserMapper.insert(chatSessionUser);
        }else{
            chatSessionUserMapper.update(chatSessionUser,chatSessionUserQueryWrapper);
        }
        return userInfo;//只是顺带的返回一个刚好要用到的
    }


    public void insertOrUpdateUserContact(UserContact userContact,String applyUserId,String contactId){
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.eq("user_id", applyUserId);
        userContactQueryWrapper.eq("contact_id", contactId);
        Integer integer = userContactMapper.selectCount(userContactQueryWrapper);
        System.out.println(integer);
        if (integer==null||integer==0){
            userContactMapper.insert(userContact);
        }else{
            userContactMapper.updateById(userContact);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addContact4Robot(String applyUserId){
        Date curDate = new Date();
        SysSettingDto sysSettingDto = redisComponent.getSysSetting();
        String robotUid = sysSettingDto.getRobotUid();
        String robotNickName = sysSettingDto.getRobotNickName();
        String sendMessage = sysSettingDto.getRobotWelcome();
        sendMessage = StringTools.cleanHtmlTag(sendMessage);
        //添加机器人好友
        UserContact userContact = new UserContact();
        userContact.setUserId(applyUserId);
        userContact.setContactId(robotUid);
        userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
        userContact.setContactType(UserContactTypeEnum.USER.getType());
        userContactMapper.insert(userContact);
        //增加会话信息
        //参数前后顺序无关  因为设置了排序后再进行拼接的
        String sessionId = StringTools.getChatSessionId4User(new String[]{applyUserId, robotUid});
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastMessage(sendMessage);
        chatSession.setLastReceiveTime(System.currentTimeMillis());
        chatSessionMapper.insert(chatSession);
        //增加会话人信息
        ChatSessionUser chatSessionUser = new ChatSessionUser();
        chatSessionUser.setUserId(applyUserId);
        chatSessionUser.setSessionId(sessionId);
        chatSessionUser.setContactName(robotNickName);
        chatSessionUser.setContactId(robotUid);
        chatSessionUserMapper.insert(chatSessionUser);
        //增加聊天消息信息
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setContactId(applyUserId);
        chatMessage.setSendUserId(robotUid);
        chatMessage.setStatus(MessageStatusEnum.FINISHED.getStatus());
        chatMessage.setContactType(UserContactTypeEnum.USER.getType());
        chatMessage.setMessageContent(sendMessage);
        chatMessage.setSendTime(System.currentTimeMillis());
        chatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
        chatMessage.setSessionId(sessionId);
        chatMessage.setSendUserNickName(robotNickName);
        chatMessageMapper.insert(chatMessage);


    }

}
