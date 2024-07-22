
package com.tikchat.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tikchat.annotation.GlobalInterceptor;
import com.tikchat.entity.GroupInfo;
import com.tikchat.entity.UserContactApply;
import com.tikchat.entity.UserInfo;
import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.entity.dto.UserContactApplyDto;
import com.tikchat.entity.dto.UserContactSearchResultDto;
import com.tikchat.entity.enums.PageSize;
import com.tikchat.entity.enums.ResponseCodeEnum;
import com.tikchat.entity.enums.UserContactStatusEnum;
import com.tikchat.entity.enums.UserContactTypeEnum;
import com.tikchat.entity.vo.ResponseVO;
import com.tikchat.entity.vo.UserInfoVO;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.GroupInfoMapper;
import com.tikchat.mapper.UserContactApplyMapper;
import com.tikchat.mapper.UserContactMapper;
import com.tikchat.mapper.UserInfoMapper;
import com.tikchat.service.UserContactApplyService;
import com.tikchat.service.UserInfoService;
import com.tikchat.service.component.ServiceComponent;
import com.tikchat.utils.CopyTools;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;


import com.tikchat.service.UserContactService;
import com.tikchat.entity.UserContact;


/**
 * <p>
 * 联系人 前端控制器
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
@RestController("userContactController")
@RequestMapping("/contact")
@Validated
public class UserContactController extends ABaseControllertest{
    @Resource
    private UserContactService userContactService;

    @Resource
    private UserInfoService userInfoService;

    @Resource
    private UserContactApplyService userContactApplyService;

    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private UserContactApplyMapper userContactApplyMapper;

    @Resource
    private ServiceComponent serviceComponent;

    @Resource
    private UserInfoMapper userInfoMapper;

    @Resource
    private GroupInfoMapper groupInfoMapper;



    @RequestMapping("/singleSearch")
    @GlobalInterceptor
    @Transactional(rollbackFor = Exception.class)
    public ResponseVO search(HttpServletRequest req,
                             @NotEmpty String contactId){
//        if(Id.startsWith(UserContactTypeEnum.USER.getPrefix())){
//            //通过userId查
//
//        }else if (Id.startsWith(UserContactTypeEnum.GROUP.getPrefix())){
//            //通过groupId查
//
//        }else{
//
//        }
        //直接所有都差才好
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        //搜索contactId 用户或这群聊
        UserContactSearchResultDto resultDto = userContactService.searchContact(tokenUserInfoDto.getUserId(), contactId);
        System.out.println("他妈的");
        return getSuccessResponseVO(resultDto);
    }


    @RequestMapping("/applyAdd")
    @GlobalInterceptor
    public ResponseVO applyAdd(HttpServletRequest req,
                               @NotEmpty String contactId,
                               String applyInfo){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        Integer joinType = userContactApplyService.applyAdd(tokenUserInfoDto, contactId, applyInfo);
        //根据joinType响应石佛u直接添加成功或者是等待验证完成
        return getSuccessResponseVO(joinType);
    }


    //获取receive_user_id的 别人申请的列表
    @RequestMapping("/loadApply")
    @GlobalInterceptor
    public ResponseVO loadApply(HttpServletRequest req,Integer pageNo){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);

        Page<UserContactApply> page = new Page<>(pageNo, PageSize.SIZE15.getSize());
//        Integer joinType = userContactService.loadApply(tokenUserInfoDto, contactId, applyInfo);
        String userId = tokenUserInfoDto.getUserId();
        //根据receive_user_id来查这个人有多少收到的申请  不管是什么类型的都要显示

        //这里没有sql注入风险  所以可以直接随便.last  还是得好好学一下mybatis  写写xml！！！！！
//        QueryWrapper<UserContactApply> userContactApplyQueryWrapper = new QueryWrapper<>();
//        userContactApplyQueryWrapper
////                .leftJoin("")
//                .select("user_contact_apply.*")
//        .last(" LEFT JOIN user_info u on u.user_id=user_contact_apply.apply_user_id LEFT JOIN group_info g on g.group_owner_id= user_contact_apply.contact_id where receive_user_id='U12456235485'");

//        String sql = "SELECT user_contact_apply.*,u.nick_name, " +
//                "CASE WHEN user_contact_apply.contact_type = 1 THEN g.group_name END AS groupName  FROM `user_contact_apply " +
//                "LEFT JOIN user_info u on u.user_id=user_contact_apply.apply_user_id " +
//                "LEFT JOIN group_info g on g.group_id= user_contact_apply.contact_id " +
//                "where receive_user_id=#{receive_user_id} Order by update_time Desc";


//        QueryWrapper<UserContactApply> userContactApplyQueryWrapper = new QueryWrapper<>();
//        userContactApplyQueryWrapper.last("SELECT user_contact_apply.*,u.nick_name, " +
//                "CASE WHEN user_contact_apply.contact_type = 1 THEN g.group_name END AS groupName  FROM `user_contact_apply " +
//                "LEFT JOIN user_info u on u.user_id=user_contact_apply.apply_user_id " +
//                "LEFT JOIN group_info g on g.group_id= user_contact_apply.contact_id " +
//                "where receive_user_id='U90003571017' Order by last_apply_time Desc");

//        userContactApplyQueryWrapper.select("user_contact_apply.*, u.nick_name, " +
//                        "CASE WHEN user_contact_apply.contact_type = 1 THEN g.group_name END AS groupName")
//                .leftJoin("user_info u", "u.user_id = user_contact_apply.apply_user_id")
//                .leftJoin("group_info g", "g.group_id = user_contact_apply.contact_id")
//                .eq("receive_user_id", receive_user_id)
//                .orderByDesc("update_time");
//        userContactApplyQueryWrapper.orderByDesc("update_time");

//                .select("user_contact_apply.*","u.nick_name",
//                        "CASE WHEN user_contact_apply.contact_type = 1 THEN g.group_name END AS group_name")
//                .last(" LEFT JOIN user_info u ON u.user_id = user_contact_apply.apply_user_id" + " LEFT JOIN group_info g ON g.group_owner_id = user_contact_apply.contact_id")
//                .eq("receive_user_id", userId)
//                .orderByDesc("update_time");


//                .
//                .last("WHERE u.create_time >= '2023-01-01'");

        //传过前端去的信息还要有群名和申请人的nickName  或者只需要申请人的nickName
        //所以要多表查询  多表连接
//        List<UserContactApply> userContactApplies = userContactApplyMapper.selectPage(page,userContactApplyQueryWrapper).getRecords();
//        List<UserContactApply> userContactApplies = userContactApplyMapper.selectList(userContactApplyQueryWrapper);
        //本来老罗是使用的PaginationResultVO来返回信息的  但是我觉得这个无所谓的 你前端本来就会传pageNo过来，那么我还把这个传回去就没必要了p
        //所以前端还要做相应的一点改变！！！！！

        List<UserContactApplyDto> userContactApplyDtos = userContactApplyMapper.selectContactApplyListByReceiveUserId(userId);

        userContactApplyDtos = userContactApplyDtos.subList((pageNo-1)*PageSize.SIZE15.getSize(),userContactApplyDtos.size());

        //总结  mp的多表查询太傻逼了
        return getSuccessResponseVO(userContactApplyDtos);
    }


    @RequestMapping("/dealWithApply")
    @GlobalInterceptor
    public ResponseVO dealWithApply(HttpServletRequest req, @NotNull Integer applyId,@NotNull Integer status){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        //操作applyId修改该条信息 修改其中的status字段就行 当然还有version和update_time
        userContactApplyService.dealWithApply(tokenUserInfoDto,applyId,status);

        //总结  mp的多表查询太傻逼了
        return getSuccessResponseVO(null);
    }


    //分别加载2种联系人列表  前端传入的参数是： 前端是分两个地方显示  一个是显示群组的contact列表  一个是显示联系人好友的列表
    @RequestMapping("/loadContact")
    @GlobalInterceptor
    public ResponseVO loadContact(HttpServletRequest req, @NotNull String contactType){
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByName(contactType);
        if (typeEnum==null){
            throw new BusinessException(ResponseCodeEnum.CODE_600.getMsg());
        }

        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        String userId = tokenUserInfoDto.getUserId();
        //根据主人的userId来在userContact表中找有哪些好友
//        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
//        userContactQueryWrapper.eq("user_id", userId);
//        userContactQueryWrapper.eq("contact_type", typeEnum);
        if (typeEnum.equals(UserContactTypeEnum.USER)){
            List<UserInfo> Infos = userContactMapper.selectUserInfoListByUserIdFromUserContact(userId);
            return getSuccessResponseVO(Infos);
        }else{
            List<GroupInfo> Infos = userContactMapper.selectGroupInfoListByUserIdFromUserContact(userId);
            return getSuccessResponseVO(Infos);
        }

//        //但是还要查群名或者人名  所以要连接两个表
//        List<UserContact> userContacts = userContactMapper.selectList(userContactQueryWrapper);
    }

    // 获取联系人base信息 不一定是好友  可以在群里看其他人的那种
    @RequestMapping("/getUserInfoBase")
    @GlobalInterceptor
    public ResponseVO getUserInfoBase(HttpServletRequest req, @NotNull String userId){
        //TODO 我觉得这里还要判断userId是不是群id 要不然怕出错
        //我在此已做改进：
        UserContactTypeEnum typeEnum = UserContactTypeEnum.getByPrefix(userId);
        if (UserContactTypeEnum.USER!=typeEnum){
            //参数错误 传入的userId不是user的Id 格式不对
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        String userId1 = tokenUserInfoDto.getUserId();
        //能进这个函数说明已经过了登陆校验的  所以不用管这个req里面token对应的userId对不对
        UserInfo userInfo = userInfoMapper.selectById(userId);
        UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
        userInfoVO.setContactStatus(UserContactStatusEnum.NOT_FRIEND.getStatus());
        //查看你们是不是好友  如果是好友的话显示 ”发送消息“  不是好友的话 显示 “添加好友”
        UserContact userContact = serviceComponent.selectByUserIdAndContactId(userId, userId1);
        //必须限定是USER关系才能返回这种userInfoVO的信息
        if (userContact!=null && UserContactTypeEnum.USER.getType().equals(userContact.getContactType())){
            if (ArrayUtils.contains(new Integer[]{
                    UserContactStatusEnum.BLACK_FRIEND.getStatus(),
                    UserContactStatusEnum.DELETED_FRIEND.getStatus(),
                    UserContactStatusEnum.NOT_FRIEND.getStatus(),
            }, userContact.getStatus())){
                //只有满足上面的要求才算是好友关系  其他的一律视为非好友
                userInfoVO.setContactStatus(UserContactStatusEnum.FRIEND.getStatus());
            }
        }
        return getSuccessResponseVO(userInfoVO);
    }


    //获取联系人base信息 只有好友才能获取  同时我和老罗的不一样  我这个还可以返回群的info
    @RequestMapping("/getContactInfo")
    //   需要写一个/getUserInfoDetail
    @GlobalInterceptor
    public ResponseVO getContactInfo(HttpServletRequest req, @NotNull String contactId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        String userId = tokenUserInfoDto.getUserId();
        UserContact userContact = serviceComponent.selectByUserIdAndContactId(userId, contactId);
        if (userContact!=null){
            if (ArrayUtils.contains(new Integer[]{
                    UserContactStatusEnum.FRIEND.getStatus(),
                    UserContactStatusEnum.FRIEND_DELETED_ME.getStatus(),
                    UserContactStatusEnum.FRIEND_BLACK_ME.getStatus(),
//                    UserContactStatusEnum.FRIEND_BLACK_ME_FIRST.getStatus(),
            },userContact.getStatus())){
                // 说明有这个人 并且是 好友 或者 非第一次被拉黑 或 被删除
                //所以可以给我看他的信息了  因为是看联系人 所以能看很详细的内容
                if (UserContactTypeEnum.USER.getType().equals(userContact.getContactType())){
                    UserInfo userInfo = userInfoMapper.selectById(contactId);
                    UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
                    userInfoVO.setContactStatus(userContact.getStatus());
//                    userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
                    return getSuccessResponseVO(userInfoVO);
                }else if (UserContactTypeEnum.GROUP.getType().equals(userContact.getContactType())){
                    GroupInfo groupInfo = groupInfoMapper.selectById(contactId);
//                    UserInfoVO userInfoVO = CopyTools.copy(userInfo, UserInfoVO.class);
//                    userInfoVO.setAdmin(tokenUserInfoDto.getAdmin());
                    System.out.println("他妈的");
                    return getSuccessResponseVO(groupInfo);
                }
            }
        }
        return getSuccessResponseVO(null);
    }

    //删除联系人
    @RequestMapping("/delContact")
    @GlobalInterceptor
    public ResponseVO delContact(HttpServletRequest req, @NotNull String contactId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        String userId = tokenUserInfoDto.getUserId();
        userContactService.removeUserContact(userId,contactId,UserContactStatusEnum.DELETED_FRIEND);
        return getSuccessResponseVO(null);
    }
    /*上面这个和下面这个代码一样 只是一个是DELETED_FRIEND 一个是*/
    //拉黑联系人
    @RequestMapping("/blackContact")
    @GlobalInterceptor
    public ResponseVO blackContact(HttpServletRequest req, @NotNull String contactId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        String userId = tokenUserInfoDto.getUserId();
        userContactService.removeUserContact(userId,contactId,UserContactStatusEnum.BLACK_FRIEND);
        return getSuccessResponseVO(null);
    }

}



