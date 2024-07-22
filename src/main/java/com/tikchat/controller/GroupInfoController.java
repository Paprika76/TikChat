
package com.tikchat.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tikchat.annotation.GlobalInterceptor;
import com.tikchat.entity.UserContact;
import com.tikchat.entity.UserInfo;
import com.tikchat.entity.dto.TokenUserInfoDto;
import com.tikchat.entity.enums.*;
import com.tikchat.entity.vo.GroupInfoVO;
import com.tikchat.entity.vo.ResponseVO;
import com.tikchat.exception.BusinessException;
import com.tikchat.mapper.GroupInfoMapper;
import com.tikchat.mapper.UserContactMapper;
import com.tikchat.service.component.ServiceComponent;
import com.tikchat.utils.CopyTools;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.List;


import com.tikchat.service.GroupInfoService;
import com.tikchat.entity.GroupInfo;


import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Paprika
 * @since 2024-06-24
 */
@RestController("groupInfoController")
@RequestMapping("/group")
@Validated
public class GroupInfoController extends ABaseControllertest{
    @Resource
    private GroupInfoService groupInfoService;

    @Resource
    private GroupInfoMapper groupInfoMapper;

//    @Resource
//    private UserContactS;

    @Resource
    private UserContactMapper userContactMapper;

//    @Resource
//    private UserContactMapper userContactMapper;

    @Resource
    private ServiceComponent serviceComponent;

    @RequestMapping("/saveGroup")
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO saveGroup(HttpServletRequest req,
                                String groupId, //前端真的可以把groupid传过来吗？  我觉得有问题吧？
                                //我们不是在groupInfoService里面进一步生成新的random的id吗？
                                @NotEmpty String groupName,
                                String groupNotice,
                                @NotNull Integer joinType,
                                MultipartFile avatarFile,
                                MultipartFile avatarCover) throws IOException {
        //根据请求头里的token参数来获取token对应的user的tokenUserInfoDto 有少部分字段在其中写了 比如user_id nick_name 是否admin
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        GroupInfo groupInfo = new GroupInfo();
        groupInfo.setGroupId(groupId);//前端真的可以把groupid传过来吗？  我觉得有问题吧？
        groupInfo.setGroupName(groupName);
        groupInfo.setGroupNotice(groupNotice);
        groupInfo.setJoinType(joinType);
        groupInfo.setGroupOwnerId(tokenUserInfoDto.getUserId());
        //我们会在groupInfoService里面具体的生成

        System.out.println(groupName+groupNotice+joinType.toString());
        System.out.println(avatarFile);
        System.out.println(avatarCover);
//        String param1 = requestBody.get("param1");
//        String param2 = requestBody.get("param2");

        groupInfoService.saveGroup(groupInfo,avatarFile,avatarCover);

        return getSuccessResponseVO(null);
    }

    @RequestMapping("/loadMyGroups")//获取属于我的群聊列表
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO loadGroups(HttpServletRequest req) {
        //根据请求头里的token参数来获取token对应的user的tokenUserInfoDto 有少部分字段在其中写了 比如user_id nick_name 是否admin
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);//从Request中的token中获取token再通过token获取userInfoDto在获取userId
        //从userContact表中查询 userId有没有这个contactId 有的话说名这个user有这个contact
        QueryWrapper<GroupInfo> groupInfoQueryWrapper = new QueryWrapper<>();
        groupInfoQueryWrapper.eq("group_owner_id",tokenUserInfoDto.getUserId());
        groupInfoQueryWrapper.orderByDesc("update_time");
        List<GroupInfo> groupInfos = groupInfoMapper.selectList(groupInfoQueryWrapper);


        return getSuccessResponseVO(groupInfos);
    }

    @RequestMapping("/getMyGroupInfo")//获取某个我的群聊的详情
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getGroupInfo(HttpServletRequest req,
                                   @NotEmpty String groupId) {
        GroupInfo groupInfo = getGroupDetailCommon(req, groupId);
        //获取到了groupInfo 但是没有成员人数显示 如果是在具体的群聊里面那就是显示具体的成员列表了
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.eq("contact_id", groupId);
        Integer memberCount = userContactMapper.selectCount(userContactQueryWrapper);

//        System.out.println("groupInfo:"+groupInfo);
        GroupInfoVO groupInfoVO = CopyTools.copy(groupInfo, GroupInfoVO.class);
        groupInfoVO.setMemberCount(memberCount);//这样返回給前端的data对象就有这个值了
//        System.out.println("groupInfoVO:"+groupInfoVO);

        return getSuccessResponseVO(groupInfoVO);
    }


    private GroupInfo getGroupDetailCommon(HttpServletRequest req,String groupId){//获取我的群聊的详情中的基础信息 还有少数信息写在上面这个方法了
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);//从Request中的token中获取token再通过token获取userInfoDto在获取userId

        //法一：
        //第一个查询
        UserContact userContact = serviceComponent.selectByUserIdAndContactId(tokenUserInfoDto.getUserId(),groupId);
        if (userContact == null || !UserContactStatusEnum.FRIEND.getStatus().equals(userContact.getStatus())){
            throw new BusinessException("你不在群聊或群聊不存在或群聊已解散");
        }
        /**
         * //经过上面的查询才能通过校验   但是实际上查询我的群组是不需要去另一个表查的 不需要去user_contact表里去查询
         * //只需要查groupInfo就行  因为这是我的群 对于我来说status只有“好友”一种可能  其他是不可能的！！！！！
         * //因此我们使用下面这种方式可以减少一次查询！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
         */
        GroupInfo groupInfo = groupInfoMapper.selectById(groupId);
        //第二个查询
//        QueryWrapper<GroupInfo> groupInfoQueryWrapper = new QueryWrapper<>();
//        //两个限定条件都满足才说明这个群聊是你的群聊 再看
//        groupInfoQueryWrapper.eq("group_owner_id",tokenUserInfoDto.getUserId());
//        groupInfoQueryWrapper.eq("group_id",groupId);

//        //法二：查询一次就行  减轻运行压力
//        QueryWrapper<GroupInfo> groupInfoQueryWrapper = new QueryWrapper<>();
//        groupInfoQueryWrapper.eq("group_owner_id", tokenUserInfoDto.getUserId());
//        groupInfoQueryWrapper.eq("group_id", groupId);
//        GroupInfo groupInfo = groupInfoMapper.selectOne(groupInfoQueryWrapper);

        groupInfo = serviceComponent.selectByGroup_idAndGroup_owner_id(groupId,tokenUserInfoDto.getUserId());
        if (groupInfo == null || !GroupInfoStatusEnum.NORMAL.getStatus().equals(groupInfo.getStatus())){
//        if (groupInfo == null){//问：需要看是否解散吗？   解散后还能看信息吗？
            throw new BusinessException("你不在群聊或群聊不存在或群聊已解散");
        }
        return groupInfo;
    }


    @RequestMapping("/getGroupInfo4Chat")//获取某个我的群聊的详情
    @GlobalInterceptor(checkLogin = true)
    public ResponseVO getGroupInfo4Chat(HttpServletRequest req,
                                   @NotEmpty String groupId) {
        GroupInfo groupInfo = getGroupDetailCommon(req, groupId);
        //获取到了groupInfo 但是没有成员人数显示 如果是在具体的群聊里面那就是显示具体的成员列表了
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.eq("contact_id", groupId);
        Integer memberCount = userContactMapper.selectCount(userContactQueryWrapper);

        GroupInfoVO groupInfoVO = CopyTools.copy(groupInfo, GroupInfoVO.class);
//        GroupInfoVO groupInfoVO = new GroupInfoVO();
//        groupInfoVO.setGroupInfo(groupInfo);
        groupInfoVO.setMemberCount(memberCount);//这样返回給前端的data对象就有这个值了
        //群成员信息！！！！！！
//        List<UserContact> userContactList = userContactMapper.selectList(userContactQueryWrapper);
        UserContact userContact = userContactMapper.selectOne(userContactQueryWrapper);

        groupInfoVO.setUserContact(userContact);

//        List<String> g04459690405 = userContactMapper.selectUserIdsByContactId("G04459690405");
//        System.out.println("g04459690405:"+g04459690405);
        // 群成员列表
        List<UserInfo> userInfoList = userContactMapper.selectUserInfoListByContactId("G04459690405");
//        System.out.println("userInfos:"+userInfoList);
        groupInfoVO.setUserInfoList(userInfoList);
//        userContactMapper.selectUserInfosByContactId(g04459690405)
        return getSuccessResponseVO(groupInfoVO);
    }

    @RequestMapping("addOrRemoveGroupUser")
    @GlobalInterceptor
    /* 添加群成员或者移除群成员 */
    public ResponseVO addOrRemoveGroupUser(HttpServletRequest req,
                                           @NotNull String groupId,
                                           @NotNull String selectContact,
                                           @NotNull Integer opType){//op是操作的意思   操作类型
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        groupInfoService.addOrRemoveGroupUser(tokenUserInfoDto,groupId,selectContact,opType);


        return getSuccessResponseVO(null);
    }

    @RequestMapping("leaveGroup")
    @GlobalInterceptor
    /* 添加群成员或者移除群成员 */
    public ResponseVO leaveGroup(HttpServletRequest req,
                                           @NotNull String groupId,
                                           @NotNull String selectContact,
                                           @NotNull Integer opType){//op是操作的意思   操作类型
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);
        groupInfoService.leaveGroup(tokenUserInfoDto.getUserId(),groupId, MessageTypeEnum.LEAVE_GROUP);
        return getSuccessResponseVO(null);
    }

    @RequestMapping("/dissoluteGroup")
    @GlobalInterceptor
    /*注意这里写了一个自动的参数  老罗没有*/
    public ResponseVO dissoluteGroup(HttpServletRequest req,
                                     @NotEmpty String groupId){
        TokenUserInfoDto tokenUserInfoDto = getTokenUserInfoDto(req);

        //注意这个方法在群主解散群聊的地方也要用！！ 所以为了封装  就这样写了
        groupInfoService.dissoluteGroup(tokenUserInfoDto.getUserId(),groupId);
        return getSuccessResponseVO(null);
    }


}

