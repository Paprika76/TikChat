package com.tikchat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tikchat.entity.UserContact;
import com.tikchat.entity.UserInfo;
import com.tikchat.entity.UserInfoBeauty;
import com.tikchat.entity.config.AppConfig;
import com.tikchat.entity.constants.Constants;
import com.tikchat.entity.dto.MessageSendDto;
import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.entity.enums.*;
import com.tikchat.entity.vo.UserInfoVO;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.UserContactMapper;
import com.tikchat.mapper.UserInfoBeautyMapper;
import com.tikchat.mapper.UserInfoMapper;
import com.tikchat.redis.RedisComponent;
import com.tikchat.service.ChatSessionUserService;
import com.tikchat.service.UserContactService;
import com.tikchat.service.UserInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tikchat.utils.CopyTools;
import com.tikchat.utils.StringTools;
import com.tikchat.websocket.MessageHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户信息表 服务实现类
 * </p>
 *
 * @author Paprika
 * @since 2024-06-22
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements UserInfoService {
    //可以发现 在controller里面操作的是更前线的东西，而service是我们内部数据的东西 数据库中的数据操作和某些具体业务逻辑
    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private UserInfoBeautyMapper userInfoBeautyMapper;

    @Resource
    private AppConfig appConfig;

    @Resource
    private RedisComponent redisComponent;

    @Resource
    private UserContactService userContactService;

    @Resource
    private ChatSessionUserService chatSessionUserService;

    @Resource
    private MessageHandler messageHandler;

    public UserInfo selectUserInfoByEmail(String email){
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        return userInfoMapper.selectOne(wrapper);
    }
    @Override
    public UserInfo selectUserInfoById(String id){
        return userInfoMapper.selectById(id);
    }




    public UserInfoBeauty selectUserInfoBeautyByEmail(String email){
        QueryWrapper<UserInfoBeauty> wrapper = new QueryWrapper<>();
        wrapper.eq("email", email);
        return userInfoBeautyMapper.selectOne(wrapper);
    }

//    @Override //下面有改进版！！！！！  这个版本做了没用的返回值  每个方法都返回 再在controller里判断这样太麻烦了
//    // 直接全局throw BusinessException就行 这样的话会自动携带errorCode errorMsg!!!!!!!!!!!!!!!!
//    public ResultMap<Void> register(String email, String nickName, String password) {
//            UserInfo userInfo = selectUserInfoByEmail(email);
//        if (userInfo == null){
////            objectResultMap.setInfo("sucess");
//            String userId = StringTools.getRandomUserId();
//            UserInfoBeauty userInfoBeauty = selectUserInfoBeautyByEmail(email);
//            boolean useBeauty = userInfoBeauty!=null && BeautyAccountStatusEnum.NO_USE.getStatus()==userInfoBeauty.getStatus();
//            if (useBeauty){
//                userId = UserContactTypeEnum.USER.getPrefix() + userInfoBeauty.getUserId();
//                //说明是靓号 那么两个表都要update！！！
//            }
//            //接下来创建一行的信息，用于注册插入到表中  并且更新beauty的status字段
//            UserInfo userInfo1 = new UserInfo();
//            userInfo1.setUserId(userId);
//            userInfo1.setEmail(email);
//            userInfo1.setPassword(StringTools.encodeMd5(password));
//            userInfo1.setNickName(nickName);
//            userInfo1.setStatus(UserStatusEnum.ENABLE.isStatus());
//            userInfo1.setLastOffTime(new Date().getTime());
//            userInfoMapper.insert(userInfo1);
//            if (useBeauty){
//                userInfoBeauty.setStatus(BeautyAccountStatusEnum.USED.getStatus());
//                userInfoBeautyMapper.updateById(userInfoBeauty);
//            }
//

//            return new ResultMap<>(true, "注册成功！");
//        }else{
//            return new ResultMap<>(false, "邮箱已经存在！");
//
//        }
//    }

    @Override //下面有改进版！！！！！  这个版本做了没用的返回值  每个方法都返回 再在controller里判断这样太麻烦了
    // 直接全局throw BusinessException就行 这样的话会自动携带errorCode errorMsg!!!!!!!!!!!!!!!!
    @Transactional(rollbackFor = Exception.class)//不管出现什么Exception都会回滚  保持数据的一致性
    public void register(String email, String nickName, String password) {
        UserInfo userInfo = selectUserInfoByEmail(email);

        if (userInfo != null){
            throw new BusinessException("邮箱账号已经存在");
        }

//            objectResultMap.setInfo("sucess");
        String userId = StringTools.getRandomUserId();
        UserInfoBeauty userInfoBeauty = selectUserInfoBeautyByEmail(email);
        boolean useBeauty = userInfoBeauty!=null && BeautyAccountStatusEnum.NO_USE.getStatus()==userInfoBeauty.getStatus();
        if (useBeauty){
            userId = UserContactTypeEnum.USER.getPrefix() + userInfoBeauty.getUserId();
            //说明是靓号 那么两个表都要update！！！
        }
        //接下来创建一行的信息，用于注册插入到表中  并且更新beauty的status字段
        UserInfo userInfo1 = new UserInfo();
        userInfo1.setUserId(userId);
        userInfo1.setEmail(email);
        userInfo1.setPassword(StringTools.encodeMd5(password));
        userInfo1.setNickName(nickName);
        userInfo1.setStatus(UserStatusEnum.ENABLE.getStatus());
        userInfo1.setLastOffTime(new Date().getTime());
        userInfo1.setJoinType(JoinTypeEnum.APPLY.getType());
        userInfoMapper.insert(userInfo1);
        if (useBeauty){
            userInfoBeauty.setStatus(BeautyAccountStatusEnum.USED.getStatus());
            userInfoBeautyMapper.updateById(userInfoBeauty);
        }

        //TODO 已完成 创建机器人好友
        userContactService.addContact4Robot(userId);

    }


//    @Override //下面有改进版！！！！！  这个版本做了没用的返回值  每个方法都返回 再在controller里判断这样太麻烦了
//    // 直接全局throw BusinessException就行 这样的话会自动携带errorCode errorMsg!!!!!!!!!!!!!!!!
//    public ResultMap<Void> login(String email, String password) {
//        UserInfo userInfo = selectUserInfoByEmail(email);
//        //如果全部if else的嵌套判断  很难看 很难维护  而且很长  所以我们采用截断式！！！！！！！！！！！！！！！！
//        //改进版写在下面了的login了！！！！！
//        if(userInfo==null){
//            return new ResultMap<>(false, "账号不存在，请先注册！");
//        }else{
//            if (!userInfo.getPassword().equals(password)){
//                return new ResultMap<>(false, "密码错误！");
//            }else{
//                if(!userInfo.getStatus().equals(UserStatusEnum.ENABLE.isStatus())){
//                    return new ResultMap<>(false, "账号已禁用");
//                }else{
//                    //TODO 把登陆和登陆后需要的东西都塞到一个对象里去
//                    //TODO 1.查询 群组 和 2.联系人
//
//                    //TODO 3.登陆身份和登陆信息校验  浏览器有session 就一般用session 其他形式就用自己创建的对象
//                    TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(userInfo);
//
//                }
//            }
//        }
//    }

    @Override //改进版！！！！！
    public UserInfoVO login(String email, String password) {
        UserInfo userInfo = selectUserInfoByEmail(email);
        //如果全部if else的嵌套判断  很难看 很难维护  而且很长  所以我们采用截断式！！！！！！！！！！！！！！！！
        //改进版写在下面了的login了！！！！！
        if(userInfo==null || !userInfo.getPassword().equals(password)){
            throw new BusinessException("账号不存在或密码错误");
        }
        if(userInfo.getStatus().equals(UserStatusEnum.DISABLE.getStatus())){
            throw new BusinessException("账号已禁用");
        }



        //TODO 已做完! 把登陆和登陆后需要的东西都塞到一个对象里去
        //TODO 已做完! 1.查询 群组 和 2.我的联系人   也就是查询所有联系人列表  然后放入redis缓存里去  以便于后面发送消息

        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.eq("status", UserContactStatusEnum.FRIEND.getStatus());
        userContactQueryWrapper.eq("user_id", userInfo.getUserId());
        List<UserContact> userContacts = userContactMapper.selectList(userContactQueryWrapper);

        /*注意以下的固定写法  map()函数就是这样用  .stream().map(item->{return}).collect(Collectors.toList())*/
        List<String> contactIdList = userContacts.stream()
                                                .map(item->item.getContactId())
                                                .collect(Collectors.toList());
        //其实不用写这个都行  因为一定有机器人好友   不为空的话 那么就删除所有联系人 再添加进去
        // 因为这个是登录嘛  相当于是每次登录都会更新服务器中redis中存储的你的联系人列表
        if(!contactIdList.isEmpty()){
            redisComponent.clearContactIdList(userInfo.getUserId());
            redisComponent.addContactIdList(userInfo.getUserId(),contactIdList);
        }


        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(userInfo);
        //TODO 3.登陆身份和登陆信息校验  浏览器有session 就一般用session 其他形式就用自己创建的对象并放入redis中去
        Long lastHeartBeat = redisComponent.getUserHeartBeat(userInfo.getUserId());
        if (lastHeartBeat!=null){//只要心跳还在就说明没下线
            throw new BusinessException("你的账号已经在别处登陆，请推出后再尝试！");
        }
        // token校验 判断token是对的 那么就让其心跳 否则就退出心跳  强制下线
        //其实就是jwt  生成一个不可能重复的token即可 12+20位 即32位的长度
        String token = StringTools.encodeMd5(tokenUserInfoDto.getUserId() + StringTools.getRandomString(Constants.LENGTH_20));
        //保存token登录信息到redis中  因为这个是客户端不是浏览器网页 所以我们设置token失效时间短一点  2天
        //--  我自己的看法：最好是像网站一样  定期设置token的失效时间  比如每天第一次进来就视为登陆一次 重新设置token时间为2天 --
        tokenUserInfoDto.setToken(token);
        //然后通过redis保存token 或者通过session来保存！！
        //存的格式：redis:      Constants.REDIS_KEY_WS_TOKEN+tokenUserInfoDto.getToken():   tokenUserInfoDto   存放时间为1天
        //   tikchat:ws:token: tokenUserInfoDto对象  存放时间为1天
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        //修改用户的最后登陆时间
        userInfo.setLastLoginTime(System.currentTimeMillis());
        userInfoMapper.selectById(userInfo);

        //将Dto转为controller需要返回给前端的VO对象  本来需要在controller里面转换的  但是为了减少一次查询userInfo信息的查询次数  所以直接在这里
//        //返回一个有关用户所有信息的VO
//        //首先先把userInfo的信息复制给VO对象：
//        UserInfo userInfo = userInfoService.selectUserInfoById(tokenUserInfoDto.getUserId());
        //将userInfo的属性值复制到相应的一样属性名的userInfoVO对象中去  还有userInfoVO独有的属性则我们专门再赋值！！！！
        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);//!!!!!!!!!!!!!!!!
        userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
        userInfoVO.setToken(tokenUserInfoDto.getToken());

        return userInfoVO;
    }




    public TokenUserInfoDto getTokenUserInfoDto(UserInfo userInfo){
        TokenUserInfoDto tokenUserInfoDto = new TokenUserInfoDto();
        tokenUserInfoDto.setUserId(userInfo.getUserId());
        tokenUserInfoDto.setNickName(userInfo.getNickName());
        //获取管理员email判断这个人是不是管理员
        String adminEmails = appConfig.getAdminEmails();
        String[] adminArray = adminEmails.split(",");
        System.out.println(adminArray);
        System.out.println(userInfo.getEmail());
        if(!StringTools.isEmpty(adminEmails) && ArrayUtils.contains(adminArray,userInfo.getEmail())){
            tokenUserInfoDto.setAdmin(true);
        }else{
            tokenUserInfoDto.setAdmin(false);
        }
        return tokenUserInfoDto;
    }


    // 首次编写时间 2024-06-29 03:51
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserInfo(UserInfo userInfo, MultipartFile avatarFile, MultipartFile avatarCover) throws IOException {
        if (avatarFile!=null){
            String baseFolder = appConfig.getProjectFolder() + Constants.FILE_FOLDER_FILE;
            File targetFileFolder = new File(baseFolder+Constants.FILE_FOLDER_AVATAR_NAME);
            if(!targetFileFolder.exists()){
                targetFileFolder.mkdirs();
            }
            String filePath = targetFileFolder.getPath()+"/"+userInfo.getUserId()+Constants.IMAGE_SUFFIX;
            avatarFile.transferTo(new File(filePath));
            avatarFile.transferTo(new File(filePath+Constants.COVER_IMAGE_SUFFIX));
        }

        //其实隐含了一个其他地方的更新
        /*会话列表中会显示你的昵称  那是另一个表了 具体为什么要另一个表就要再思考了！*/
        //所以我们再判断昵称有没有改变 改变了的话那就要更新另一个表的数据
        //在更新数据前先获取一下之前的昵称
        UserInfo dbInfo = userInfoMapper.selectById(userInfo.getUserId());
        String pastNickName = dbInfo.getNickName();
        //更新传过来的数据
//        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
//        userInfoQueryWrapper.eq()
        this.userInfoMapper.updateById(userInfo);
        String nickNameUpdate = null;
        if(pastNickName.equals(userInfo.getNickName())){
            nickNameUpdate = userInfo.getNickName();
        }
        if(nickNameUpdate==null){
            return ;
        }

        //更新redis tokenUserInfoDto中的nickName信息
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfoDtoByUserId(userInfo.getUserId());
        tokenUserInfoDto.setNickName(nickNameUpdate);
//        tokenUserInfoDtoByUserId
        redisComponent.saveTokenUserInfoDto(tokenUserInfoDto);

        //TODO 已完成 更新会话中的昵称信息
        chatSessionUserService.updateRedundantInfo(nickNameUpdate,userInfo.getUserId());


    }

    @Override
    public void updateUserStatus(Integer status, String userId) {
        UserStatusEnum statusEnum = UserStatusEnum.getByStatus(status);
        if(statusEnum==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        UserInfo userInfo = userInfoMapper.selectById(userId);
        if (userInfo==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        userInfo.setStatus(status);
        userInfoMapper.updateById(userInfo);
    }

    @Override
    public void forceOffLine(String userId) {
        //要用到ws强制下线 以后再写
        MessageSendDto messageSendDto = new MessageSendDto();
        messageSendDto.setContactType(UserContactTypeEnum.GROUP.getType());
        messageSendDto.setMessageContent(MessageTypeEnum.FORCE_OFFLINE.getInitMessage());
        messageSendDto.setContactId(userId);
        messageHandler.sendMessage(messageSendDto);//messageHandler里面写了了下线后的断开channel

    }



}
