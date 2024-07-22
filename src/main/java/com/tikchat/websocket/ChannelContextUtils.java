package com.tikchat.websocket;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tikchat.entity.*;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.dto.MessageSendDto;
import com.tikchat.entity.dto.WsInitData;
import com.tikchat.entity.enums.*;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.*;
import com.tikchat.redis.RedisComponent;
import com.tikchat.service.ChatMessageService;
import com.tikchat.service.ChatSessionService;
import com.tikchat.utils.JsonUtils;
import com.tikchat.utils.StringTools;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


/**
 * @FileName ChannelContextUtils
 * @Description ws 通道工具类
 * @Author Paprika
 * @date 编写时间 2024-06-30 23:11
 **/


// 编写时间 2024-07-15 20:57
@Component
public class ChannelContextUtils {

    //ConcurrentHashMap 高效的线程安全哈希表实现，适用于多线程环境下需要高并发访问的数据结构。
    private static final ConcurrentHashMap<String,Channel> USER_CONTEXT_MAP = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, ChannelGroup> GROUP_CONTEXT_MAP = new ConcurrentHashMap<>();

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ChatMessageService chatMessageService;

    @Resource
    private ChatSessionMapper chatSessionMapper;

    @Resource
    private ChatSessionService chatSessionService;

//    @Resource
//    private ChatSessionUserService chatSessionUserService;

    @Resource
    private ChatSessionUserMapper chatSessionUserMapper;

    @Resource
    private UserContactApplyMapper userContactApplyMapper;




    //作用是 已经有了channel  接下来我们把他们集成起来  便于使用
    // 添加一个通道供好友们使用  发消息使用
    public void addContext(String userId, Channel channel){//这是一登录 就会进行的操作 把用户添加到管道channel里去
        /*AttributeKey详解: 是netty中的一个用于在 Channel 或 ChannelHandlerContext 上存储和检索自定义数据的机制。*/
        //AttributeKey 提供的存储是线程安全的，这意味着你可以在不同的事件处理方法中存储和访问数据，而不必担心并发问题。
        String channelId = channel.id().toString();
        AttributeKey attributeKey = null;
        if (!AttributeKey.exists(channelId)) {
            //注意这个AttributeKey.exists()的用法
            // 是用来判断是否为Channel 或 ChannelHandlerContext 定义了特定的 AttributeKey
            attributeKey = AttributeKey.newInstance(channelId);
        }else{
            //获取channelId得到的attributeKey
            //获取到后我们对相应的channel    channel.attr(attributeKey).set(userId)
            //获取到后attributeKey.get()能获取到
            attributeKey = AttributeKey.valueOf(channelId);
        }
        //以下就是设置了只要有channel就能获取到channelId 就能获取到相应的userId
        channel.attr(attributeKey).set(userId);//就相当于说有一个和本channelId绑定的
        //注意channel.attr(attributeKey)其实是一个Attribute对象了 这个对象其实就是一个键值对!!!!!!!!!
        //简单讲解他妈的AttributeKey  为什么不直接使用channelId来当key 而是使用AttributeKey来当key
        // （当然 value就是我们想要的userId嘛）

        /*总结来说，尽管在某些简单的情况下直接使用 channelId 可能更直观，
        但是使用 AttributeKey 可以带来更好的代码清晰度、可读性和维护性，同时在性能上也有优势。
        在大多数情况下，Netty 设计使用 AttributeKey 是为了优化和提高代码的可理解性和性能。*/


        //获取当前当事人 即本人的“联系人列表”  然后添加到管道里去，这样发消息就方便了 发消息也是
        //然后发消息时就能根据userId来看在哪个管道发消息
        // 如果是好友 人的话 那就发送到userId对应的User_channel管道里去  然后就会对该管道内的人发消息
        // 如果是群组       那就发送到userId对应的群组的Group_channel管道里去   然后就会对所有人发消息
        /*从redis中获取本人对饮的联系人列表*/
        List<String> contactIdList = redisComponent.getContactIdList(userId);
        for (String contactId:contactIdList){
            if(contactId.startsWith(UserContactTypeEnum.GROUP.getPrefix())){
                add2Group(contactId,channel);
            }
        }

        //这个的意思是在ConcurrentHashMap类型的user的map中添加了一个用户
        // 这里的用户全部都是在线的用户  也就是能接收消息 能发送消息
        USER_CONTEXT_MAP.put(userId,channel);

        //添加完联系人后就更新本人的心跳 让其确实是在线状态
        redisComponent.saveUserHeartBeat(userId);//疑问: 这个不应该写在上一级的代码中吗
        // 不应该写在HandlerWebsocket的userEventTriggered函数里吗


        //发送消息 开始使用  三个消息表了！！！
        //更新本用户最后登录时间
        UserInfo userInfo1 = userInfoMapper.selectById(userId);
        userInfo1.setLastLoginTime(System.currentTimeMillis());
        userInfoMapper.updateById(userInfo1);

        //给用户发消息 但是只获取没有接受过的
        // 老罗的是：并且是7天内的消息   不然你很久很久没登陆了 还收到到消息就很搞笑
        // 但是我觉得可以收消息啊  我们只需要定期去删除半个月或者15天以前的消息就行啊！！！
        /*其实啊 本来就是一个消息就是7天或者15天就一定会在服务端删除的！！！！ 这时候本来就查不到啊  所以我们会自动去删除*/
        //TODO 写一个自动删除程序  每天在凌晨3-5点间删除所有7天前的消息 写在另一个自动化模块中去!!!!!
        // 登进去也还是能收到没接受过的信息   因为如果有一个人发了消息给你
        // 但是你在坐牢或者其他事登不了qq  出狱后登qq发现一个找你的都没  会颠掉
        // 所以这个自动程序
        // 然后这里也是要限定7天内的消息才能接收  超过时间就不能接收了 不  这是一个bug
        // 我们应该限定就算这个用户1年没登陆了  也还是能收到消息
        // ---这个自动程序真的要做的应该是: ①发送了 并且用户已经接收了 那么就是7天自动从数据库中删除所有消息
        // --------------------------- ②发送了 但是用户没登陆 没接收,那么我们让其自动 半年 删除!!!!!!!
        // ----- TODO 因为我们还要搞一个新功能:查看聊天记录: 这个的话那就是请求7天之前的消息就请求不到了
        //  ---而我们在这里的第一次接收消息就是需要判断这个消息是不是半年内的  是半年内的才能接收!!!!!!!
        /*总结: 自动删除聊天记录程序: 一种7天的情况 一种半年的情况
        *              这里要写的代码功能: 第一次获取消息: 半年内的消息!!!!!!!*/

        // 刚登进来 我们获取没登陆时别人发给我们的消息  并且要是半年内的消息才行!!
//        Long lastOffTime = userInfo1.getLastOffTime();
        Long lastOffTime = userInfo1.getLastOffTime();
        Long oldestQueryTime = lastOffTime;//最多查询7天前的消息时间
        Long oldestReceiveTime = lastOffTime;//最多接收半年前的消息的时间
        if(lastOffTime!=null&&System.currentTimeMillis()-Constants.MILLISECOND_HALF_YEAR>lastOffTime){
            oldestReceiveTime = System.currentTimeMillis()-Constants.MILLISECOND_HALF_YEAR;//半年前的时间
        }
        if(lastOffTime!=null&&System.currentTimeMillis()-Constants.MILLISECOND_7days>lastOffTime){
            oldestQueryTime = System.currentTimeMillis()-Constants.MILLISECOND_7days;//7天前的时间
        }
        /*老罗写了上面的代码  但是我觉得没用   每次就都是查半年前的消息就行
        * 不对!!!!!  有点用!!区别就是性能问题   如果没半年时间的话  那我们就查到上次登录的时间的
        *  毕竟这是一个大型查询  查消息很多的 等于是进行了一个很小的优化 很小很小的优化*/

        //但是在查询消息之前  首先我们得先把会话列表获取一下
        /**
         * 1、查询会话信息 查询用户所有的会话信息,保证换了设备后会话还是同步的    这个直接查所有的 没有任何限制条件
         */
//        QueryWrapper<ChatSessionUser> chatSessionUserQueryWrapper = new QueryWrapper<>();
//        chatSessionUserQueryWrapper.eq("user_id", userId);
    // "last_receive_time"这个字段是chat_session表里的  所以要多表联查  所以我们用mapper自定义写sql
//        chatSessionUserQueryWrapper.orderByDesc("last_receive_time");
//        List<ChatSessionUser> chatSessionList = chatSessionUserMapper.selectList(chatSessionUserQueryWrapper);
        List<ChatSessionUser> chatSessionList = chatSessionUserMapper.selectChatSessionListByUserId(userId);

        WsInitData wsInitData = new WsInitData();
        wsInitData.setchatSessionList(chatSessionList);

        /**
         * 2、查询聊天消息  注意这里不是查询所有的最后一条信息
         * 而是直接查所有的能获取的时间期限内的所有人的聊天记录 要是每次点击一个人就查询和这个人的所有消息的话
         * 就会太多查询了  而每次登录就把和所有联系人的所有信息都查一遍这样后面有人发消息就再存和发
         *
         * 具体该怎么样我也不咋清楚 唉 都行吧    我觉得还是只查最后一条消息就行   然后点击进去后再查对应的所有消息
         */
        //TODO 这里写的很不好  老罗这样写我感觉很不好  待改进！！！！！！！！！！！！
        List<String> groupIdList = contactIdList.stream().filter(item -> item.startsWith(UserContactTypeEnum.GROUP.getPrefix())).collect(Collectors.toList());
        groupIdList.add(userId);//其实就是一个包含所有群+自己（有了自己就有了所有的非群的好友的对话）
        QueryWrapper<ChatMessage> chatMessageQueryWrapper = new QueryWrapper<>();
        Long finalOldestQueryTime = oldestQueryTime;
        chatMessageQueryWrapper.and(i->i.gt("send_time", finalOldestQueryTime).eq("status", MessageStatusEnum.SENDING.getStatus()));
        Long finalOldestReceiveTime = oldestReceiveTime;
        chatMessageQueryWrapper.or(i->i.gt("send_time", finalOldestReceiveTime).eq("status", MessageStatusEnum.FINISHED.getStatus()));
//        chatMessageQueryWrapper.gt("send_time",oldestQueryTime);
//        chatMessageQueryWrapper.eq("status", MessageStatusEnum.SENDING.getStatus());//只接收正在发送的 说明没接收到的
        chatMessageQueryWrapper.in("contact_id", groupIdList);
        List<ChatMessage> chatMessageList = chatMessageMapper.selectList(chatMessageQueryWrapper);
        wsInitData.setChatMessageList(chatMessageList);
        /**
         * 3、查询好友申请
         */
//        //
        QueryWrapper<UserContactApply> friendApplyQueryWrapper = new QueryWrapper<>();
        friendApplyQueryWrapper.eq("receive_user_id", userId);
        friendApplyQueryWrapper.eq("status", UserContactApplyStatusEnum.INIT.getStatus());
        friendApplyQueryWrapper.gt("last_apply_time", oldestReceiveTime);  //这个有点巧面  要细细琢磨一下为什么
        Integer applyCount = userContactApplyMapper.selectCount(friendApplyQueryWrapper);
        wsInitData.setApplyCount(applyCount);

        /**
         * 发送消息给刚登录进来的这个用户  其实这个消息就是list 其实这里不能写dto了  就是直接传给前端的了  直接就是VO了
         * 这是ws通信 不是http通信  所以看起来没那么有层次  直接一次性做了很多事情  ws讲究的就是不断地发消息接收消息
         * 当然这里直接用http也是可以的   毕竟就是登陆的时候才会发送这个请求 才会需要这些信息
         * 以后写代码都可以先用http的思路走一遍再改成ws的  因为这是ws 更有实时性!!!!!
         * 这样看来其实就不复杂了   我们在这的目的就是获取到会话列表 显示在刚进去的会话消息列表画面
         *  并且需要每个会话的基础信息 比如会话名(contact_name) last_message 和群组的人数  除了这个还有需要
         */
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setMessageType(MessageTypeEnum.INIT.getType());
        messageSendDto.setContactId(userId);//表示这个消息是要发给userId的
        messageSendDto.setExtendData(wsInitData);//刚登进来的时候获取很多表面信息

        sendMsg(messageSendDto,userId);


        //TODO 接下来就是获取不在线时别人给你发的消息了
        // 这个不需要通过redis来 因为发消息不那么不频繁  所以必须要建表
        // 会话表 消息表 会话映射表(会话用户表)(发送人 接收人)(这个和UserContact是很像的 因为都是互相的!!!!  1->2  2->1)
        // 要有多个表  因为涉及多个操作: 比如这1.里需要获取不在线时别人给你发的消息
        // 2.在表里查询判断消息有没有接收
        // (1)《会话表》:因为会话我们可以删除 就算删除了会话 但是联系人也没有被删除  所以和联系人表是不一样的
        // 字段:session_id last_message last_receive_time create_time update_time deleted version
        // 作用: update_time用于搞会话列表的顺序  特别注意主要就是这两个字段:last_message和last_receive_time
        // (2)《消息表》: 消息的id  属于哪个会话(也就是会话id)  消息内容(内容限制 1000字) 发送时间     消息类型  文件类型 文件信息(暂时不知道怎么做
        // 看老罗怎么操作了)还有消息状态(0:正在发 1:已发送)  联系人类型Type(群组 好友)
        // 发送人的Id 和 昵称(这个我想不通!!!  我觉得很可能是空间换时间的一种做法了
        // 不知道为什么需要发送人的昵称  实际是不用的 可能有其他非常规功能 再看吧 无大碍)   接收人的Id
        // 字段 message_id session_id message_content send_user_id receive_user_id
        // (3)《会话映射表》(会话用户表)(发送人 接收人)(这个和UserContact是很像的 因为都是互相的!!!!  1->2  2->1)
        // 其实就是会话的一些其他额外的信息  比如说会话表表里没有的联系人的名称  其实这个你可以把它看成是你自己备注的信息  哦不是
        // TODO 备注的功能应该是这样写的   直接在userContact里面修改对应的人的名字！！！！！
        //  字段：user_id contact_id session_id     contact_name!!!!!!!!!!
        //  其实就是为了让会话表中的session_id能查到contact_name
        //  最主要的功能是 user_id查询contact_id和查询session_id  因为会话表和消息表都是他们自己在玩  没有user_id
        //  所以我们就需要将user_id和 “消息的这两个表” 绑定起来 场景：用户加载会话信息时需要这两个表！！！



//        //(如果是群聊的话) 把该在线的用户添加到相应的group的channel里面 后面有人发消息的时候那么这个管道里面的所有人都会收到消息
//        add2Group("10000",channel);




    }

    //发送消息
    /*前置条件:messageSendDto已经包含了所有需要的信息!!!!!!!!!  才能调用这个方法*/
    public void sendMsg(MessageSendDto messageSendDto,String receiveUserId){
//        channel.writeAndFlush(new TextWebSocketFrame(message));
        if (receiveUserId==null){
            return;
        }
//        Channel sendChannel = null;
        if(UserContactTypeEnum.USER.equals(UserContactTypeEnum.getByPrefix(messageSendDto.getContactId()))){
            Channel userChannel = USER_CONTEXT_MAP.get(receiveUserId);
            userChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));
            /* 特别注意这里用户收到消息：有很多情况：
            * 1.用户发消息给他
            * 2.”群“发消息给他 比如：“xxx已经加入了群聊，欢迎他”
            * “你已经加入了群聊，和大家打个招呼吧”
            * “群已解散” “xxx已退出群聊” “xxx被移除群聊”
            * 3.                待做*/  //TODO 发消息给用户的形式很多种
            //我看了整个发送消息的过程 发现我们直接就是  最关键的就是MessageTypeEnum包含了很多很多消息类型


        }else if (UserContactTypeEnum.GROUP.equals(UserContactTypeEnum.getByPrefix(messageSendDto.getContactId()))){
            ChannelGroup groupChannel = GROUP_CONTEXT_MAP.get(receiveUserId);
            groupChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.convertObj2Json(messageSendDto)));
        }
//        return ;
//        if (sendChannel==null){
//            return;
//        }
//        channel.writeAndFlush(new TextWebSocketFrame(messageSendDto));
        //正式发消息  设置要发的消息的内容体   但是其中的接收者也就是ContactId要改成发送者
        // 其实就是转成前端更友好一点点的数据  因为前端不需要你的sendUserId
        // 都已经给我了 那肯定我只需要ContactId也就是会话列表中对应联系人的信息就行
        /* 这里注意如果是别人发消息给另一个人 那么dto里写的是 a->b  老罗写的是这里转了一下  但是我觉得不用转  */
        // TODO 到底要不要互换身份
//        if(!MessageTypeEnum.ADD_FRIEND.getType().equals(messageSendDto.getMessageType())){
//            messageSendDto.setContactId(messageSendDto.getSendUserId());
//            messageSendDto.setContactName(messageSendDto.getSendUserNickName());
//        }

    }

    public void sendMessage(MessageSendDto messageSendDto){
        UserContactTypeEnum messageContactType = UserContactTypeEnum.getByPrefix(messageSendDto.getContactId());
        switch (messageContactType){
            case USER://单聊 发送给单个用户
                send2User(messageSendDto);
                break;
            case GROUP://群聊 发送给群里的所有用户
                send2Group(messageSendDto);
                break;
            default:
                throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
    }


    //单聊 发送给单个用户  发给contactId这个人就行
    public void send2User(MessageSendDto messageSendDto){
        String contactId = messageSendDto.getContactId();
        sendMsg(messageSendDto,contactId);
        //之所以要分开写也是因为这后面还要进行一些操作 比如说强制下线操作!!!!    文件发送等等的处理
        if(MessageTypeEnum.FORCE_OFFLINE.getType().equals(messageSendDto.getMessageType())){
            //TODO 已完成 强制下线
            setChannelOffLine(contactId);
        }
    }

    //群聊 发送给群里的所有用户 messageSendDto里面是contactId是群发
    // 发给所有contactId是这个的 对应的userId的人
    // 在chat_message_user表里 所有contactId是这个群Id的所有userId  使用send2User(userId)
    public void send2Group(MessageSendDto messageSendDto){
        String contactId = messageSendDto.getContactId();
        ChannelGroup groupChannel = GROUP_CONTEXT_MAP.get(contactId);
        sendMsg(messageSendDto,contactId);

        //移除群聊的话那就还要把groupChannel关了
        //1.退出群聊 或 把人移出群聊
        MessageTypeEnum messageTypeEnum = MessageTypeEnum.getByType(messageSendDto.getMessageType());
        if (MessageTypeEnum.LEAVE_GROUP==messageTypeEnum||MessageTypeEnum.REMOVE_USER_FROM_GROUP==messageTypeEnum){
            //退出群聊或者被移除群聊！！！
            String userId = (String ) messageSendDto.getExtendData();
            //redis中删掉这个人
            redisComponent.removeContactIdFromList(userId, contactId);
            //groupChanel中删掉这个人
            Channel channel = USER_CONTEXT_MAP.get(userId);
            if (channel==null){
                return ;
            }
            groupChannel.remove(channel);
        }
        //2.或者是 解散群聊  那就直接把groupChannel关了！！！ redis中把这个list关了 把每个人都删掉
        else if (MessageTypeEnum.DISSOLUTE_GROUP==messageTypeEnum){
            GROUP_CONTEXT_MAP.remove(messageSendDto.getContactId());
            groupChannel.close();
            // TODO 疑问：redis中要删掉相关信息吗
        }
    }


    //
    public void add2Group(String groupId,Channel channel){
        ChannelGroup groupChannel = GROUP_CONTEXT_MAP.get(groupId);
        if (groupChannel==null) {
            groupChannel = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId,groupChannel);
        }
        if (channel == null) {
            return;
        }
        groupChannel.add(channel);//说明已经有群组了   群组里有很多个channel
    }
    public void add2Group(String groupId,String userId){
        ChannelGroup groupChannel = GROUP_CONTEXT_MAP.get(groupId);
        if (groupChannel==null) {
            groupChannel = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_CONTEXT_MAP.put(groupId,groupChannel);
        }
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if (channel == null) {
            return;
        }
        groupChannel.add(channel);//说明已经有群组了   群组里有很多个channel
    }

//
//    public void send2Group(String message){
//        ChannelGroup groupChannel = GROUP_CONTEXT_MAP.get("10000");
//        //给群组管道里的所有人都发消息
//        groupChannel.writeAndFlush(new TextWebSocketFrame(message));
//    }




    public static String getUserId(Channel channel){
        Attribute<String> attribute = channel.attr((AttributeKey.valueOf(channel.id().toString())));
        //注意这里通过channel.attr()获取到一个Attribute对象  这个对象其实就是一个键值对!!!!!!!!!
        return attribute.get();
    }

    public void removeChannel(Channel channel){
        String userId = getUserId(channel);
//        if(!StringTools.isEmpty(userId)){
//            GROUP_CONTEXT_MAP.remove(userId);//会删除userId对应的channel
//        }
        GROUP_CONTEXT_MAP.remove(userId);//会删除userId对应的channel
        redisComponent.removeUserHeartBeat(userId);//接收消息的channel 正式下线
//        forceOffLine(userId);//老罗没这样写 但是我觉得要这样写
        UserInfo userInfo = userInfoMapper.selectById(userId);
//        System.out.println("他妈的userId:"+userId);
        if (userInfo != null) {
            //修改用户的最后下线时间
            userInfo.setLastOffTime(System.currentTimeMillis());
            userInfoMapper.updateById(userInfo);
        }
    }
    public void setChannelOffLine(String userId){// TODO 下线 后面还要再封装一下  搞一个主动下线  然后在这个函数forceOffLine里调用主动下线
        if (StringTools.isEmpty(userId)){
            return ;
        }
        Channel channel = USER_CONTEXT_MAP.get(userId);
        if(channel==null){
            return ;
        }
        removeChannel(channel);
        //把用户的登录的2天有效期的token也搞掉
        redisComponent.removeTokenUserInfoDto(userId);

    }


}
