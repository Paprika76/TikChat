package com.tikchat.service.component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.tikchat.entity.GroupInfo;
import com.tikchat.entity.UserContact;
import com.tikchat.entity.UserContactApply;
import com.tikchat.mapper.GroupInfoMapper;
import com.tikchat.mapper.UserContactApplyMapper;
import com.tikchat.mapper.UserContactMapper;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class ServiceComponent {
    @Resource
    private UserContactMapper userContactMapper;

    @Resource
    private GroupInfoMapper groupInfoMapper;

    @Resource
    private UserContactApplyMapper userContactApplyMapper;


    //判断是不是好友contact_id是不是我的好友
    public UserContact selectByUserIdAndContactId(String user_id,String contact_id){
        QueryWrapper<UserContact> userContactQueryWrapper = new QueryWrapper<>();
        userContactQueryWrapper.eq("user_id", user_id);
        userContactQueryWrapper.eq("contact_id", contact_id);
        return userContactMapper.selectOne(userContactQueryWrapper);
    }

    //判断是不是好友contact_id是不是我的好友
    public GroupInfo selectByGroup_idAndGroup_owner_id(String group_id,String group_owner_id){
        QueryWrapper<GroupInfo> groupInfoQueryWrapper = new QueryWrapper<>();
        groupInfoQueryWrapper.eq("group_id", group_id);
        groupInfoQueryWrapper.eq("group_owner_id", group_owner_id);
        return groupInfoMapper.selectOne(groupInfoQueryWrapper);
    }

    public UserContactApply selectByApplyUserIdAndReceiveUserIdAndContactId(String applyUserId, String receiveUserIdA, String contactId){
        QueryWrapper<UserContactApply> groupInfoQueryWrapper = new QueryWrapper<>();
        groupInfoQueryWrapper.eq("apply_user_id", applyUserId);
        groupInfoQueryWrapper.eq("receive_user_id", receiveUserIdA);
        groupInfoQueryWrapper.eq("contact_id", contactId);
        return userContactApplyMapper.selectOne(groupInfoQueryWrapper);
    }



}
