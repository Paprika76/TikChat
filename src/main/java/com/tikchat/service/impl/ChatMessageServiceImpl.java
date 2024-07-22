package com.tikchat.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tikchat.controller.AGlobalExceptionHandlerController;
import com.tikchat.entity.ChatMessage;
import com.tikchat.entity.ChatSession;
import com.tikchat.entity.UserContact;
import com.tikchat.entity.config.AppConfig;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.dto.MessageSendDto;
import com.tikchat.entity.dto.SysSettingDto;
import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.entity.enums.*;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.ChatMessageMapper;
import com.tikchat.mapper.ChatSessionMapper;
import com.tikchat.mapper.UserContactMapper;
import com.tikchat.redis.RedisComponent;
import com.tikchat.service.ChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tikchat.utils.CopyTools;
import com.tikchat.utils.StringTools;
import com.tikchat.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 聊天消息表 服务实现类
 * </p>
 *
 * @author Paprika
 * @since 2024-07-16
 */
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatMessageService {

    private static final Logger logger = LoggerFactory.getLogger(AGlobalExceptionHandlerController.class);

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private MessageHandler messageHandler;

    @Resource
    private AppConfig appConfig;


    //某人主动发消息给别人
    @Override
    public MessageSendDto saveMessage(ChatMessage chatMessage, TokenUserInfoDto tokenUserInfoDto) {
        //这个是主动发消息给别人  自己也要收到的那种  但是： 机器人他不用登录 所以就不用返回信息给机器人了
        //所以要排除掉机器人
        /*判断是不是好友 不是好友就要拦截*/
        if(!Constants.ROBOT_UID.equals(tokenUserInfoDto.getUserId())){
            //查看redis缓存里面  你有没有对方这个联系人
            List<String> contactIdList = redisComponent.getContactIdList(tokenUserInfoDto.getUserId());//设计不合理  应该改为set数据结构  而不是list
            System.out.println("他妈的");
            for(String s:contactIdList){
                System.out.println(s);
            }
            if (!contactIdList.contains(chatMessage.getContactId())) {
                UserContactTypeEnum userContactTypeEnum = UserContactTypeEnum.getByPrefix(chatMessage.getContactId());
                if(UserContactTypeEnum.USER==userContactTypeEnum){
                    //一种是和用户聊时 判断出你不是好友
                    throw new BusinessException(ResponseCodeEnum.CODE_902);
                }else{
                    //一种是群聊时 判断出你不是群里的人
                    throw new BusinessException(ResponseCodeEnum.CODE_903);
                }
            }
        }

        //开始保存消息到 消息表中去了！！！！
        String sessionId = null;//注意这里不是messageId  messageId才是真正的主键！！！！！  自动加一！！！！！！
        String sendUserId = tokenUserInfoDto.getUserId();
        String contactId = chatMessage.getContactId();
        Long curTime = System.currentTimeMillis();

        chatMessage.setSendUserId(sendUserId);
        chatMessage.setSendUserNickName(tokenUserInfoDto.getNickName());
//        chatMessage.setContactId(contactId);
        chatMessage.setSendTime(curTime);
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if(UserContactTypeEnum.USER==contactTypeEnum){
            sessionId = StringTools.getChatSessionId4User(new String[]{contactId,sendUserId});
        }else{//群
            sessionId = StringTools.getChatSessionId4Group(contactId);
        }
        chatMessage.setSessionId(sessionId);

        //处理消息的状态  只要不是文件消息就是直接设为发送成功的消息
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(chatMessage.getMessageType());
        if(null==messageTypeEnum || !ArrayUtils.contains(new Integer[]{
                MessageTypeEnum.CHAT.getType(),
                MessageTypeEnum.MEDIA_CHAT.getType()
        },chatMessage.getMessageType())) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        Integer status = MessageTypeEnum.MEDIA_CHAT==messageTypeEnum? MessageStatusEnum.SENDING.getStatus() :
                MessageStatusEnum.FINISHED.getStatus();
        chatMessage.setStatus(status);

        String messageContent = StringTools.cleanHtmlTag(chatMessage.getMessageContent());
        chatMessage.setMessageContent(messageContent);

        chatMessage.setContactType(contactTypeEnum.getType());

        chatMessageMapper.insert(chatMessage);

        //更新会话-ChatSession表  修改最后一条消息
        ChatSession chatSession = new ChatSession();
        chatSession.setLastMessage(messageContent);
        if(UserContactTypeEnum.GROUP==contactTypeEnum){
            chatSession.setLastMessage(tokenUserInfoDto.getNickName()+":"+messageContent);
        }
        chatSession.setLastReceiveTime(curTime);
        QueryWrapper<ChatSession> chatSessionQueryWrapper = new QueryWrapper<>();
        chatSessionQueryWrapper.eq("session_id", sessionId);
        chatSessionMapper.update(chatSession,chatSessionQueryWrapper);


        //ws发消息
        MessageSendDto messageSendDto = CopyTools.copy(chatMessage,MessageSendDto.class);
        //如果是机器人的话直接在这里再返回消息给联系人  （本来这个应该专门开一个端口来作为所有人的机器人好友的！！！  ）
        if(Constants.ROBOT_UID.equals(contactId)){
            //机器人不需要真的接收你的消息  所以你不需要真的发送消息
            // 只需要机器人发消息给你就行  因为机器人其实就是在本服务器里 所以不用转发给他
            /*回复AI回复的信息*/
            //从配置中获取机器人的各种配置信息
            SysSettingDto sysSettingDto = redisComponent.getSysSetting();
            TokenUserInfoDto robot = new TokenUserInfoDto();
            robot.setNickName(sysSettingDto.getRobotNickName());
            robot.setUserId(sysSettingDto.getRobotUid());
            ChatMessage robotChatMessage = new ChatMessage();
            robotChatMessage.setContactId(sendUserId);
            robotChatMessage.setMessageType(MessageTypeEnum.CHAT.getType());
            robotChatMessage.setMessageContent("我只是一个机器人无法识别你的信息");
            saveMessage(robotChatMessage,robot);//这个递归了一下  机器人给刚刚的发送人发送消息直接回复

        }
        messageHandler.sendMessage(messageSendDto);
        return messageSendDto;
    }

    @Override
    public void saveMessageFile(String userId, String messageId, MultipartFile file, MultipartFile cover) {
        ChatMessage chatMessage = chatMessageMapper.selectById(messageId);
        if (chatMessage==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if(!chatMessage.getMessageId().equals(userId)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //判断文件大小
        SysSettingDto sysSetting = redisComponent.getSysSetting();
        if (file.getSize()>sysSetting.getMaxFileSize()*Constants.FILE_SIZE_MB){
            throw new BusinessException(ResponseCodeEnum.CODE_600,"文件超出发送的最大限制"+sysSetting.getMaxFileSize()+"MB");
        }

        String filename = file.getOriginalFilename();
        //判断文件是类型
        String fileSuffix = StringTools.getFileSuffix(filename);
//        fileSuffix==null||    //无文件类型的传输方式没写
        if(!StringTools.isEmpty(fileSuffix)){//不能无后缀的文件
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        if(ArrayUtils.contains(Constants.IMAGE_SUFFIX_LIST,fileSuffix.toLowerCase())){

        }else if(ArrayUtils.contains(Constants.VIDEO_SUFFIX_LIST,fileSuffix.toLowerCase())){

        }

//        String fileName = file.getOriginalFilename();
//        String fileSuffix = StringTools.getFileSuffix(fileName);
        //TODO 注意：要特别注意消息文件的命名是messageId!!!!!!!!!
        // 后面会考的 也就是用户接收文件时发送的请求中要带上这个messageId
        String fileRealName = messageId+fileSuffix;
        String folderNameOfDay = DateUtil.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        File folder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + folderNameOfDay);
        if(!folder.exists()){
            folder.mkdirs();
        }
        File uploadFile = new File(folder.getPath() + "/" + fileRealName);
        try {
            file.transferTo(uploadFile);
            cover.transferTo(new File(uploadFile.getPath()+"/"+Constants.COVER_IMAGE_SUFFIX));
        }catch (Exception e){
            logger.error("文件上传失败",e);
            throw new BusinessException("文件上传失败");
        }

        chatMessage.setStatus(MessageStatusEnum.FINISHED.getStatus());
        //乐观锁  其实我们要封装好来  使用表中的version字段
//        QueryWrapper<ChatMessage> chatMessageQueryWrapper = new QueryWrapper<>();
//        chatMessageQueryWrapper.eq("message_id", messageId);
//        chatMessageQueryWrapper.eq("version", );

        QueryWrapper<ChatMessage> chatMessageQueryWrapper2 = new QueryWrapper<>();
        chatMessageQueryWrapper2.eq("message_id", messageId);
        chatMessageQueryWrapper2.eq("status", MessageStatusEnum.SENDING.getStatus());
        chatMessageMapper.updateById(chatMessage);//这个应该自动就会乐观锁的

        //发消息
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setStatus(MessageStatusEnum.FINISHED.getStatus());
        messageSendDto.setMessageId(Long.parseLong(messageId));
        messageSendDto.setContactId(chatMessage.getContactId());
        messageSendDto.setMessageType(MessageTypeEnum.SUCCESS_UPLOAD.getType());
        messageHandler.sendMessage(messageSendDto);

    }

    @Override
    public File downloadFile(String userId, Long messageId, Boolean showCover) {
        //处理的是消息中发送的文件  fileId就是messageId
        ChatMessage chatMessage = chatMessageMapper.selectById(messageId);
        String contactId = chatMessage.getContactId();
        //我们来分析一下这里的逻辑： 通过messageId下载文件 并返回文件  如果是视频图片的话还要返回cover！！！
        //但是又要返回Cover又要返回文件 怎么做到   我觉得就是分两次呗：一次是showCover的（cover图）  一次是不showCover的（具体文件）
        //1，获取文件 通过messageId查找那条消息中的文件名 然后再在服务器中寻找这个文件名的文件返回发送
        // 如果showCover 那就文件名+后缀！！！
        // 如果不showCover 那就直接文件名
        //最后进行 ”安全排错处理“  优化代码:    判断fileId也就是messageId的正确性  所以我们先查询messageId获取一些信息  判断一些信息
        // 判断userId和这个messageId里面的contactId是不是一样的
        // （是sendUserId的话也是可以的 因为我发送的我想在其它设备下载看看也可以的）
        /*如果是好友的话 那么判断userId在不在这条message里面 是接收人或发送人都行*/
        UserContactTypeEnum contactTypeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if (UserContactTypeEnum.USER==contactTypeEnum&&(!userId.equals(contactId)||!userId.equals(chatMessage.getSendUserId()))){
            throw new BusinessException(ResponseCodeEnum.CODE_600,"你没有文件下载权限");
        }
        /*如果是群的话那就判断你在不在这个群里！！！！   并且status要是Friend状态！！！！！*/
        //可以改进  凡是要涉及容易耗性能的 都要判断能不能使用redis缓存来实现！！！
        // （反正就是进行了拉黑等等操作后都需要在redis中删除 这样在这里就能直接使用redis就不用查库了）
        if (UserContactTypeEnum.GROUP==contactTypeEnum){
            //查询群内所有的用户
            QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
            userContactQueryWrapper.eq("contact_id", contactId);
            userContactQueryWrapper.eq("user_id", userId);
            userContactQueryWrapper.eq("contact_type", UserContactTypeEnum.GROUP.getType());
            userContactQueryWrapper.eq("status", UserContactStatusEnum.FRIEND.getStatus());
            Integer contactCount = userContactMapper.selectCount(userContactQueryWrapper);
            if(contactCount==0){
                throw new BusinessException(ResponseCodeEnum.CODE_600,"你没有文件下载权限");
            }
        }


        //核心逻辑其实就这几行：

        String folderNameOfDay = DateUtil.format(new Date(chatMessage.getSendTime()), DateTimePatternEnum.YYYY_MM_DD.getPattern());
        File folder = new File(appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE + folderNameOfDay);
        if(!folder.exists()){
            folder.mkdirs();
        }
        String fileSuffix = StringTools.getFileSuffix(chatMessage.getFileName());
        String fileRealName = messageId+fileSuffix;
        if(showCover!=null&&showCover){
            fileRealName += Constants.COVER_IMAGE_SUFFIX;
        }

        File downloadFile = new File(folder.getPath() + "/" + fileRealName);
        if (!downloadFile.exists()){
            logger.info("文件不存在{}",messageId);
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }
        return downloadFile;

    }
}

