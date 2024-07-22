package com.tikchat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tikchat.entity.GroupInfo;
import com.tikchat.entity.UserContact;
import com.tikchat.entity.UserContactApply;
import com.tikchat.entity.UserInfo;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.dto.MessageSendDto;
import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.entity.enums.*;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.*;
import com.tikchat.redis.RedisComponent;
import com.tikchat.service.UserContactApplyService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tikchat.service.UserContactService;
import com.tikchat.service.component.ServiceComponent;
import com.tikchat.utils.StringTools;
import com.tikchat.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;

/**
 * <p>
 * 联系人申请 服务实现类
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
@Service
public class UserContactApplyServiceImpl extends ServiceImpl<UserContactApplyMapper, UserContactApply> implements UserContactApplyService {

    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private UserContactService userContactService;

    @Resource
    private ServiceComponent serviceComponent;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private GroupInfoMapper groupInfoMapper;

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



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer applyAdd(TokenUserInfoDto tokenUserInfoDto, String contactId, String applyInfo) {
        //判断添加的人的id对不对
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(contactId);
        if(typeEnum==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        //申请人
        String applyUserId = tokenUserInfoDto.getUserId();
        //默认申请信息
        applyInfo = StringTools.isEmpty(applyInfo)? String.format(Constants.APPLY_INFO_TEMPLATE,tokenUserInfoDto.getNickName()):applyInfo;

        //决定返回的是1还是0  根据contactId设置的joinType来判断
        //拉黑拦截  拉黑的人不能再请求添加
        //首先判断是否已经是好友 null表明不是好友
        UserContact userContact = serviceComponent.selectByUserIdAndContactId(applyUserId, contactId);
        if (userContact!=null){//至少成为过好友
            if(ArrayUtils.contains(new Integer[]{
                    UserContactStatusEnum.FRIEND_BLACK_ME.getStatus(),
                    UserContactStatusEnum.FRIEND_BLACK_ME_FIRST.getStatus()
            }, userContact.getStatus()))
                throw new BusinessException("对好已将你拉黑，无法添加");
            if(UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus()))
                throw new BusinessException("你们已经是好友，无法再申请");
            if(UserContactStatusEnum.BLACK_FRIEND.getStatus().equals(userContact.getStatus()))
                throw new BusinessException("你已将对方拉黑，请先解除拉黑再添加好友");
        }


        String receive_user_id = contactId;
        Integer joinType = null;

        //申请进入群
        if (typeEnum == UserContactTypeEnum.GROUP){
            GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
            if(groupInfo==null || GroupInfoStatusEnum.DISMISSED.getStatus().equals(groupInfo.getStatus())){
                throw new BusinessException("群聊不存在或已解散");
            }
            //发送请求给群主
            receive_user_id = groupInfo.getGroupOwnerId();
            //返回joinType让前端决定返回的页面
            joinType = groupInfo.getJoinType();
        }else{
            //申请加好友
            UserInfo userInfo = userInfoMapper.selectById(contactId);
//            if(userInfo==null || UserStatusEnum.DISABLE.getStatus().equals(userInfo.getStatus())){
            if(userInfo==null){//不用写禁用的  因为就算搜索的账号被禁用了  我们可能还是可以发申请给他  具体能不能发其实还要看禁用的类型
                // 但是我们这个系统没设置这么多类型  其实都无所谓 都行的  看具体的业务逻辑需要说嘛
                throw new BusinessException(ResponseCodeEnum.CODE_600);
            }
            joinType = userInfo.getJoinType();
        }
        //如果joinType是不用申请 那我们在这里就直接把它加完他
        //直接加入不用申请
        if(JoinTypeEnum.JOIN.getType().equals(joinType)){
            userContactService.addContact(applyUserId,receive_user_id,contactId,typeEnum.getType(),applyInfo);
            return joinType;
        }
        //在apply申请表中添加一条申请的信息
        UserContactApply dbApply = serviceComponent.selectByApplyUserIdAndReceiveUserIdAndContactId(applyUserId,receive_user_id,contactId);
        /**
         * 实际上这里只有两种可能了     REJECT(2,"已拒绝"),和    INIT(0,"待处理"),
         * 因为前面我已经写了拉黑和已经是好友的处理了  所以后面这几行就不用了  不用再判断一次
         */
        if (dbApply!=null){//至少发过一次申请
            if(UserContactApplyStatusEnum.BLACK.getStatus().equals(dbApply.getStatus()))
                throw new BusinessException("对好已将你拉黑，无法申请");
            if(UserContactApplyStatusEnum.PASS.getStatus().equals(dbApply.getStatus()))
                throw new BusinessException("申请已通过，无法再申请");
        }
        if(dbApply==null){//说明第一次发申请  如果是第二次就会直接跳过防止信息轰炸
            //真正的申请的逻辑实现代码
            //增加一条信息到user_contact_apply表里
            UserContactApply userContactApply = new UserContactApply();
            userContactApply.setContactId(contactId);
            userContactApply.setReceiveUserId(receive_user_id);
            userContactApply.setApplyUserId(applyUserId);
            userContactApply.setContactType(typeEnum.getType());//这个是申请加好友加群的类型  是加好友还是加群
            userContactApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());//后面的3个status需要被申请者即申请接收者
            // 来设置这个申请是通过还是不通过
            userContactApply.setApplyInfo(applyInfo);
//            userContactApply.setLastApplyTime(DateTimePatternEnum.getCurTimeFormatted());
            userContactApply.setLastApplyTime(new Date().getTime());
            userContactApplyMapper.insert(userContactApply);
        }else{//说明这个dbApply是存在的 已经申请过了的  只是不知道有没有被拒绝或者是拉黑还是什么的
//            //更新状态
//            UserContactApply userContactApply = new UserContactApply();
//            //到了这里只有两种可能了  REJECT(2,"已拒绝"),和    INIT(0,"待处理"),
            dbApply.setStatus(UserContactApplyStatusEnum.INIT.getStatus());//用户可能已拒绝 但是我还能再申请
            dbApply.setApplyInfo(applyInfo);
            dbApply.setLastApplyTime(new Date().getTime());
            userContactApplyMapper.updateById(dbApply);//其实就是改了一下update时间 让对方看到最新的申请时间  但是实际上只会接收到一次申请
        }
        if (dbApply==null||!UserContactApplyStatusEnum.INIT.getStatus().equals(dbApply.getStatus())){
            //即如果是第一次申请 或者：申请多次 且状态不是待处理的话那就发条消息给接收者(因为状态还是待处理的话说明用户还没有回复 所以不能又发多条消息给他申请轰炸)
            //TODO 已完成 发送ws消息给接收者  很多情况下用http也行的  只不过更慢
            // 还有就是http的话是被动的 要客户端来请求才会有反应！！！而如果是ws的话 那就可以是我服务器主动发消息给他
            // 消息类型是“好友申请”  然后前端每次接收ws消息都会判断你是什么类型的 怎么处理！！！
            /*注意每次发ws消息都是使用 messageSendDto 里面设置了很多消息类型  跟数据库的chat_message表是一样的*/
            MessageSendDto messageSendDto = new MessageSendDto();
            messageSendDto.setMessageType(MessageTypeEnum.CONTACT_APPLY.getType());
            messageSendDto.setMessageContent(applyInfo);
            messageSendDto.setSendUserId(applyUserId);
            messageSendDto.setContactId(receive_user_id);
//            channelContextUtils.sendMessage(messageSendDto);//不能直接通过channelContextUtils发消息 而是通过redisson集群发
            //在messageHandler里面每个机器都会调用channelContextUtils.sendMessage()
            // 哪个机器中有messageSendDto.contact_id  哪个就会真的发messageSendDto消息给他
            // 机器中的channel中没有这个userId的话也会调用sendMessage  只不过在sendMessage中没找到这个channel啊 所以在这个机器中就没有发送成功
            messageHandler.sendMessage(messageSendDto);

        }


        return joinType;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void dealWithApply(TokenUserInfoDto tokenUserInfoDto, Integer applyId, Integer status) {
        UserContactApplyStatusEnum statusEnum = UserContactApplyStatusEnum.getByStatus(status);

        String userId = tokenUserInfoDto.getUserId();
        //操作applyId修改该条信息 修改其中的status字段就行 当然还有version和update_time
        //1.搜索applyId在不在里面  不在的话说明没有
        UserContactApply userContactApply = userContactApplyMapper.selectById(applyId);

        if (userContactApply==null || !userContactApply.getReceiveUserId().equals(userId)){
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode());
        }
        //2.看status对不对  符不符合要求
        //不存在的status或者不合理的status  接收者处理申请 不可能把它设置为待处理 因为这太煞笔了
        if (statusEnum==null || statusEnum == UserContactApplyStatusEnum.INIT){
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode());
        }

        //3.最终让他没有漏洞地修改
        QueryWrapper<UserContactApply> userContactApplyQueryWrapper = new QueryWrapper<>();
        userContactApplyQueryWrapper.eq("status", 0);
        userContactApplyQueryWrapper.eq("apply_id", applyId);
        Integer count = userContactApplyMapper.selectCount(userContactApplyQueryWrapper);
        System.out.println("count.equals(1)");
        System.out.println(count);
        System.out.println(count.equals(1));
        if (!count.equals(1)){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        userContactApply.setStatus(statusEnum.getStatus());
        userContactApplyMapper.updateById(userContactApply);

        //4.  注意第3步已经update了contactApply表了  接下来修改其他表
        // 如果我选择申请通过  那么就添加联系人
        if (statusEnum == UserContactApplyStatusEnum.PASS){
            userContactService.addContact(userContactApply.getApplyUserId(), userContactApply.getReceiveUserId(), userContactApply.getContactId(), userContactApply.getContactType(), userContactApply.getApplyInfo());
            return;
        }

        //5.  注意第3步已经update了contactApply表了  接下来修改其他表
        // 如果不是选择添加联系人  那么就是已拒绝或者已拉黑  已拒绝不用理他直接吧
        if (statusEnum == UserContactApplyStatusEnum.BLACK){
            //双向加好友设置black
            this.addContactBlack(userContactApply.getApplyUserId(),userId,UserContactStatusEnum.FRIEND_BLACK_ME_FIRST.getStatus());
            this.addContactBlack(userId,userContactApply.getApplyUserId(),UserContactStatusEnum.BLACK_FRIEND.getStatus());
            // TODO 后面还要在其他地方写取消拉黑的操作路由 这就引申出了新的问题：
            //  如果是第一次拉黑别人  然后又取消拉黑了 那么怎么从我的列表中删除这个好友
            //  所以解除拉黑也分两种 第一种是本来是好友然后拉黑了 那么就直接修改UserContact表的status就行
            //  如果是第一次拉黑别人 那么就需要删除这个好友 不用改status了

        }

    }


    public void addContactBlack(String applyUserId,String userId,Integer status){
        UserContact userContact = new UserContact();
        userContact.setUserId(applyUserId);

        userContact.setContactId(userId);
//            userContact.setStatus(UserContactStatusEnum.FRIEND_BLACK_ME.getStatus());
        userContact.setStatus(status);
        // TODO 还有一个bug 就是我们这里只是改了申请人的设置  没有改接收人的配置：
        //  拉黑的话也要在接收人那里显示是拉黑了的status
        //  加为好友的话前面我写了addContact 是双向加好友 那么这个也要双向啊


        userContact.setContactType(UserContactTypeEnum.USER.getType());//因为拉黑只能对人进行拉黑！！！
        //对人进行拉黑后  我们需要回去看一下有没有写被拉黑后不能申请加入拉黑你的人的群的设置！！！！

        //先看一下存不存在  是不是已经是好友了 如果是好友了那就是update 不是insert！！！！！
        UserContact userContact1 = serviceComponent.selectByUserIdAndContactId(applyUserId, userId);
        if (userContact1==null){
            userContactMapper.insert(userContact);
        }else{
            QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
            userContactQueryWrapper.eq("user_id",applyUserId);
            userContactQueryWrapper.eq("contact_id",userId);
//            userContactMapper.updateById(userContact);
            userContactMapper.update(userContact,userContactQueryWrapper);
        }

    }


}
