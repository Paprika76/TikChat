package com.tikchat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tikchat.entity.*;
import com.tikchat.entity.config.AppConfig;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.dto.ChatSessionUserDto;
import com.tikchat.entity.dto.MessageSendDto;
import com.tikchat.entity.dto.SysSettingDto;
import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.entity.enums.*;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.*;
import com.tikchat.redis.RedisComponent;
import com.tikchat.service.ChatSessionUserService;
import com.tikchat.service.GroupInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tikchat.service.UserContactApplyService;
import com.tikchat.service.UserContactService;
import com.tikchat.service.component.ServiceComponent;
import com.tikchat.utils.CopyTools;
import com.tikchat.utils.StringTools;
import com.tikchat.websocket.ChannelContextUtils;
import com.tikchat.websocket.MessageHandler;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
@Service
public class GroupInfoServiceImpl extends ServiceImpl<GroupInfoMapper, GroupInfo> implements GroupInfoService {
    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private AppConfig appConfig;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private ServiceComponent serviceComponent;

    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    @Resource
    private UserContactApplyService userContactApplyService;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private UserContactService userContactService;

    @Resource
    private ChannelContextUtils channelContextUtils;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    @Lazy  /* 自己引入自己会 “循环依赖” 所以要加一个注解！！！！！！！！！！！！！意思是延缓他加载一下 */
    private GroupInfoService groupInfoService;


    @Override
    @Transactional(rollbackFor = Exception.class)//涉及多个表 所以要事务性
    public void saveGroup(GroupInfo groupInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        //新增一个groupInfo 新建一个群组
        if(StringTools.isEmpty(groupInfo.getGroupId())){
            //如果给的参数groupInfo中没包含GroupId：说明是新建一个群组！！！！！！！！

            //获取controller传过来的groupInfo信息  并判断group_owner_id这个人有多少个群组了  如果超过了sysSetting的数量那就不让他新增了
            QueryWrapper<GroupInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("group_owner_id", groupInfo.getGroupOwnerId());
            Integer count = groupInfoMapper.selectCount(queryWrapper);
            SysSettingDto sysSetting = redisComponent.getSysSetting();
            if(count>=sysSetting.getMaxGroupCount()){
                throw new BusinessException("最多只能创建"+sysSetting.getMaxGroupCount()+"个群聊！");
            }

            //必须写头像
            if(avatarFile==null){
                //前端实现传头像有点复杂 后面再实现
//                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode());
            }
            //至于还有很多其他的字段已经在controller里面从前端传过来了
            groupInfo.setGroupId(StringTools.getRandomGroupId());
            groupInfoMapper.insert(groupInfo);


            //将群组添加为我的联系人
            UserContact userContact = new UserContact();
            userContact.setStatus(UserContactStatusEnum.FRIEND.getStatus());
            userContact.setContactType(UserContactTypeEnum.GROUP.getType());
            userContact.setContactId(groupInfo.getGroupId());
            userContact.setUserId(groupInfo.getGroupOwnerId());

            userContactMapper.insert(userContact);

            //TODO 已完成 创建群组会话 多个步骤
            //1.创建会话 增加会话信息 ChatSession表
            String groupSessionId = StringTools.getChatSessionId4Group(groupInfo.getGroupId());
            ChatSession chatSession = new ChatSession();
            chatSession.setSessionId(groupSessionId);
            chatSession.setLastMessage(MessageTypeEnum.GROUP_CREAT.getInitMessage());
            chatSession.setLastReceiveTime(System.currentTimeMillis());
            ChatSession dbChatMessage = chatSessionMapper.selectById(groupSessionId);
            if(dbChatMessage==null){
                chatSessionMapper.insert(chatSession);
            }else{
                chatSessionMapper.updateById(chatSession);
            }
            // 2.增加会话人信息 ChatSessionUser表  a增加 b也增加
//            ChatSessionUser chatSessionUser = new ChatSessionUser();
//            chatSessionUser.setUserId(groupInfo.getGroupOwnerId()));
//            chatSessionUser.setContactId(groupInfo.getGroupId());
//            chatSessionUser.setContactName(groupInfo.getGroupName());
//            chatSessionUser.setSessionId(groupSessionId);
            //可以直接调用下面这个函数实现
            userContactService.addChatSessionUserAndGetRearUserInfo(groupSessionId,groupInfo.getGroupOwnerId(),groupInfo.getGroupId());

            // 3.增加聊天消息信息 ChatMessage表
            ChatMessage chatMessage = new ChatMessage();
            chatMessage.setSessionId(groupSessionId);
            chatMessage.setMessageContent(MessageTypeEnum.GROUP_CREAT.getInitMessage());
            chatMessage.setMessageType(MessageTypeEnum.GROUP_CREAT.getType());
            //这两个不用
//            chatMessage.setSendUserId(groupInfo.getGroupOwnerId());
//            chatMessage.setSendUserNickName(applyUserInfo.getNickName());
            chatMessage.setContactId(groupInfo.getGroupId());
            chatMessage.setStatus(MessageStatusEnum.FINISHED.getStatus());
            chatMessage.setSendTime(System.currentTimeMillis());
            chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
            chatMessageMapper.insert(chatMessage);


            //TODO 注意: 很多时候你都会忘记写某些代码  比如这个redis的
            // 将群组添加到联系人 缓存   反正就是只要涉及了联系人的改动  那就要修改redis  因为redis存了
            // 所以!!!!!!!!  这样:  需要一个工具来提醒我们什么做了什么  还有很多注意事项
            // 写代码时涉及到相关的逻辑就考研方便的防止漏掉!!!!!!!!!!!   其实很多逻辑都是因为容易漏掉!!!!!!!!!!
            // 非常适合写在面试 中
            redisComponent.addContactId(groupInfo.getGroupOwnerId(),groupInfo.getGroupId());// contactId可能是群主也可能是收到好友申请的人
            //将联系人添加到群组通道channel中接收消息
            // TODO 注意: 这个容易漏掉!!!!!!!   因为这个是为了要把人添加进群里才能聊天!!!  还要时刻注意参数的前后顺序
            channelContextUtils.add2Group(groupInfo.getGroupId(),groupInfo.getGroupOwnerId());



            // 4.发消息给群主  说  群已创建
            MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
            ChatSessionUserDto chatSessionUserDto = new ChatSessionUserDto();
            chatSessionUserDto.setUserId(groupInfo.getGroupOwnerId());
            chatSessionUserDto.setContactId(groupInfo.getGroupId());
            chatSessionUserDto.setContactName(groupInfo.getGroupName());
            chatSessionUserDto.setSessionId(groupSessionId);

            chatSessionUserDto.setLastReceiveTime(Long.toString(System.currentTimeMillis()));
            chatSessionUserDto.setMemberCount(1);
            messageSendDto.setExtendData(chatSessionUserDto);
            messageHandler.sendMessage(messageSendDto);//这里的MessageType就是ADD_FRIEND的



        //update修改的groupInfo信息！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
        }
        //修改群信息
        else{//如果给的参数groupInfo中包含了GroupId：说明是update群组设置信息！！！！！！！！
            //修改信息  所以我们根据groupId来从数据库中查询对应的groupInfo
            QueryWrapper<GroupInfo> groupInfoQueryWrapper = new QueryWrapper<>();
            groupInfoQueryWrapper.eq("group_id", groupInfo.getGroupId());
            GroupInfo dbGroupInfo = groupInfoMapper.selectOne(groupInfoQueryWrapper);
            //判断 db中 和 前端传过来的ownerId是不是一样  一样才能对这个Group进行修改！！
            //要不然所有人都可能可以直接修改随便哪个人的groupInfo
            if(!dbGroupInfo.getGroupOwnerId().equals(groupInfo.getGroupOwnerId())){
                throw new BusinessException(ResponseCodeEnum.CODE_600.getCode());
            }
            this.groupInfoMapper.updateById(groupInfo);
            //TODO 已完成 1.更新相关表冗余信息  其实就是更新相关表中和group有关的数据  将其改为最新的数据！！！！
            //首先看群名有没有改  其实好像也就这个值改了的话才要更新几个表！！！ 要不然只需要改GroupInfo表就行！！！！！！！
            String groupName = null;
            if (!dbGroupInfo.getGroupName().equals(groupInfo.getGroupName())){
                groupName = groupInfo.getGroupName();
            }
            if(groupName==null){
                return ;
            }
            //更改昵称
            chatSessionUserService.updateRedundantInfo(groupName,groupInfo.getGroupId());
            //TODO 已完成 或者说需要实时更新的东西就需要使用ws   --实时更新
        }


        if(avatarFile==null){
            return;
        }
        //上传头像
        String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
        File targetFileFolder = new File(baseFolder + Constants.FILE_FOLDER_AVATAR_NAME);
        if (!targetFileFolder.exists()){
            targetFileFolder.mkdirs();
        }
        String filePath = targetFileFolder.getPath() + "/" + groupInfo.getGroupId() + Constants.IMAGE_SUFFIX;
        avatarFile.transferTo(new File(filePath));
        avatarFile.transferTo(new File(filePath+Constants.COVER_IMAGE_SUFFIX));

    }

    @Override
    //这个也要考虑到是群主解散群聊的方法  所以ownerId是通过req中的token获取的 但是也有可能有别人来解散不是他own的群聊
    /*管理员解散 和 群主解散都用这个方法*/
    public void dissoluteGroup(String groupOwnerId,String groupId) {
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (groupInfo==null || !groupOwnerId.equals(groupInfo.getGroupOwnerId())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        //存在的话设置其status状态 即：解散群聊
        groupInfo.setStatus(GroupInfoStatusEnum.DISMISSED.getStatus());
        groupInfoMapper.updateById(groupInfo);

        //更新联系人信息  让所有群友的状态为”删除了这个群“ 包括群主
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.eq("contact_id", groupInfo.getGroupId());
        userContactQueryWrapper.eq("contact_type", UserContactTypeEnum.GROUP.getType());
        UserContact userContact = new UserContact();
        /*所有联系人的状态都设为删除联系人状态*/
        userContact.setStatus(UserContactStatusEnum.DELETED_FRIEND.getStatus());
        userContactMapper.update(userContact,userContactQueryWrapper);

        //TODO 已完成 删除相关群员的所有联系人缓存  这个是真的删除  而数据库中只是设定一种状态而已 可以恢复
        //1.redis缓存处理 给所有群员依次删除这个群聊联系人:他们的这个群
        List<UserContact> userContacts = userContactMapper.selectList(userContactQueryWrapper);
        for (UserContact contact : userContacts) {
            redisComponent.removeContactIdFromList(contact.getUserId(),groupId);
        }

        Date curDate = new Date();
        String messageContent = MessageTypeEnum.DISSOLUTE_GROUP.getInitMessage();
        //2.chat_session表 更新最后一条消息
        String sessionId = StringTools.getChatSessionId4Group(groupId);
        ChatSession chatSession = new ChatSession();
        chatSession.setSessionId(sessionId);
        chatSession.setLastReceiveTime(curDate.getTime());
        chatSession.setLastMessage(messageContent);
        chatSessionMapper.updateById(chatSession);

        //3.chat_message表发送一条消息  "该群已解散"
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSendUserId(sessionId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setMessageType(MessageTypeEnum.DISSOLUTE_GROUP.getType());
        chatMessage.setSendTime(curDate.getTime());
        chatMessage.setContactId(groupId);
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setStatus(MessageStatusEnum.FINISHED.getStatus());
        chatMessageMapper.insert(chatMessage);

        // TODO 已完成 发消息 1.更新绘画消息 2.记录群消息 3.发送群解散通知
        MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
        messageHandler.sendMessage(messageSendDto);

    }

    @Override
    public void addOrRemoveGroupUser(TokenUserInfoDto tokenUserInfoDto, String groupId, String groupMembersIdStr, Integer opType) {
        //判断有没有权限删除或添加群成员
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (null==groupInfo||!groupInfo.getGroupOwnerId().equals(tokenUserInfoDto.getUserId())){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        String[] groupMembersId = groupMembersIdStr.split(",");
        if(OpTypeEnum.ADD.getType().equals(opType)){
            //新增
            for (String memberId : groupMembersId) {
                userContactService.addContact(memberId,null,
                        groupId, UserContactTypeEnum.GROUP.getType(), null);
            }
        }else{
            //删除
            for (String memberId : groupMembersId) {
                // TODO 特别注意：leaveGroup是本类中的方法  直接调用leaveGroup不行  这样没有使用bean 没有交给spring管理
                //   所以需要 @Resource 引入自己才行！！！！
                groupInfoService.leaveGroup(memberId,groupId,MessageTypeEnum.REMOVE_USER_FROM_GROUP);
            }


        }



    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void leaveGroup(String userId, String groupId, MessageTypeEnum messageTypeEnum) {
        //判断有没有权限退出群聊
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        if (null==groupInfo||groupInfo.getGroupOwnerId().equals(userId)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.eq("user_id", userId);
        userContactQueryWrapper.eq("contact_id", groupId);
        int count = userContactMapper.delete(userContactQueryWrapper);
        if (count==0){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo = userInfoMapper.selectById(userId);
        //获取推出群聊的人的一些信息 如用户名  用来后面消息中设置 “某某人退出了群聊”
        String sessionId = StringTools.getChatSessionId4Group(groupId);
        Date curDate = new Date();
        String messageContent = String.format(messageTypeEnum.getInitMessage(), userInfo.getNickName());

        //又是更新消息表了！！！！
        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(messageContent);
        chatSession.setLastReceiveTime(curDate.getTime());
        chatSessionMapper.updateById(chatSession);

        //更新chatMessage表
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setSessionId(sessionId);
        chatMessage.setMessageContent(messageContent);
        chatMessage.setContactType(UserContactTypeEnum.GROUP.getType());
        chatMessage.setSendTime(curDate.getTime());
        chatMessage.setStatus(MessageStatusEnum.FINISHED.getStatus());
        chatMessage.setMessageType(messageTypeEnum.getType());
        chatMessage.setContactId(groupId);
//        chatMessage.setSendUserId(userId);  //这个没写
        chatMessageMapper.insert(chatMessage);


        QueryWrapper<UserContact> userContactQueryWrapper1 = new QueryWrapper<>();
        MessageSendDto messageSendDto = CopyTools.copy(chatMessage, MessageSendDto.class);
        messageSendDto.setMemberCount(messageSendDto.getMemberCount()-1);
        messageSendDto.setExtendData(userId);
        messageHandler.sendMessage(messageSendDto);


    }
}
